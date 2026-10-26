package cn.bit101.android.features.schedule.classroom

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.config.setting.base.TimeTableItem
import cn.bit101.android.data.repo.base.FreeClassroomRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.api.service.school.SchoolSessionExpired
import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.ClassroomInfo
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class FreeClassroomSearchViewModelTest {
    @get:Rule val liveData = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FreeClassroomRepo
    private lateinit var login: SchoolLoginRepo
    private lateinit var schedule: CourseScheduleSettings
    private lateinit var vm: FreeClassroomSearchViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk()
        login = mockk()
        schedule = mockk()
        val settings = mockk<FreeClassroomSettings>()
        every { repo.getCurrentCampusCode() } returns MutableStateFlow("01")
        every { repo.getCurrentCampusName() } returns MutableStateFlow("良乡")
        every { settings.hideBusyClassroom.flow } returns MutableStateFlow(true)
        every { settings.freeMinutesThreshold.flow } returns MutableStateFlow(5L)
        coEvery { settings.freeMinutesThreshold.get() } returns 5L
        coEvery { schedule.timeTable.get() } returns listOf(TimeTableItem(LocalTime.of(8, 0), LocalTime.of(9, 0)))
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        vm = FreeClassroomSearchViewModel(repo, login, settings, schedule)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `outer session failure becomes retryable failure instead of an uncaught coroutine exception`() = runTest(dispatcher) {
        coEvery { login.withSchoolSession<Any>(any()) } throws IOException("session recovery failed")
        vm.loadClassroomInfos("A")?.join()
        assertEquals(SimpleState.Fail, vm.getClassroomsStatesMap["A"])
        assertEquals(SimpleState.Fail, vm.getClassroomLastStatusLiveData.value)
    }
    @Test fun `entering page with building failure stays on page and manual retry works`() = runTest(dispatcher) {
        coEvery { login.withSchoolSession<List<BuildingInfo>>(any()) } coAnswers {
            firstArg<suspend () -> List<BuildingInfo>>().invoke()
        }
        coEvery { repo.getBuildingInfos("01") } throws IOException("offline")
        vm.loadBuildingTypes().join()
        assertTrue(vm.getBuildingTypeStatusLiveData.value is SimpleDataState.Fail)
        coEvery { repo.getBuildingInfos("01") } returns emptyList()
        vm.loadBuildingTypes().join()
        assertTrue(vm.getBuildingTypeStatusLiveData.value is SimpleDataState.Success)
        coVerify(exactly = 2) { repo.getBuildingInfos("01") }
    }

    @Test fun `repeated expansion shares one active classroom request`() = runTest(dispatcher) {
        val response = CompletableDeferred<List<ClassroomInfo>>()
        coEvery { repo.getClassroomInfos("A") } coAnswers { response.await() }
        val first = vm.loadClassroomInfos("A")
        repeat(10) { assertNull(vm.loadClassroomInfos("A")) }
        runCurrent()
        coVerify(exactly = 1) { repo.getClassroomInfos("A") }
        response.complete(emptyList())
        first?.join()
        assertEquals(SimpleState.Success, vm.getClassroomsStatesMap["A"])
    }

    @Test fun `failed classroom load waits for manual retry instead of repeating on UI refresh`() = runTest(dispatcher) {
        coEvery { repo.getClassroomInfos("A") } throws IOException("offline")
        vm.loadClassroomInfos("A")?.join()
        assertEquals(SimpleState.Fail, vm.getClassroomsStatesMap["A"])
        repeat(20) { vm.refreshClassroomInfoIfInvalid("A") }
        runCurrent()
        coVerify(exactly = 1) { repo.getClassroomInfos("A") }
        coEvery { repo.getClassroomInfos("A") } returns emptyList()
        vm.loadClassroomInfos("A")?.join()
        assertEquals(SimpleState.Success, vm.getClassroomsStatesMap["A"])
        coVerify(exactly = 2) { repo.getClassroomInfos("A") }
    }

    @Test fun `session expiry reaches recovery before page handles the final result`() = runTest(dispatcher) {
        coEvery { repo.getClassroomInfos("A") } throws SchoolSessionExpired() andThen emptyList()
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers {
            val operation = firstArg<suspend () -> Any>()
            try { operation() } catch (e: SchoolSessionExpired) { operation() }
        }
        vm.loadClassroomInfos("A")?.join()
        assertEquals(SimpleState.Success, vm.getClassroomsStatesMap["A"])
        coVerify(exactly = 2) { repo.getClassroomInfos("A") }
    }

    @Test fun `account invalidation after query does not publish or cache its result`() = runTest(dispatcher) {
        coEvery { repo.getClassroomInfos("A") } returns listOf(ClassroomInfo("A101", null))
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers {
            firstArg<suspend () -> Any>().invoke()
            throw CancellationException("账号已切换")
        }
        vm.loadClassroomInfos("A")?.join()
        assertNull(vm.classroomDataMap["A"])
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        vm.loadClassroomInfos("A")?.join()
        coVerify(exactly = 2) { repo.getClassroomInfos("A") }
    }

    @Test fun `malformed classroom sections and empty time tables are recoverable failures`() = runTest(dispatcher) {
        for (busy in listOf("not-a-number", "0", "99")) {
            coEvery { repo.getClassroomInfos("A") } returns listOf(ClassroomInfo("A101", busy))
            vm.loadClassroomInfos("A")?.join()
            assertEquals(SimpleState.Fail, vm.getClassroomsStatesMap["A"])
        }
        coEvery { repo.getClassroomInfos("A") } returns emptyList()
        coEvery { schedule.timeTable.get() } returns emptyList()
        vm.loadClassroomInfos("A")?.join()
        assertEquals(SimpleState.Fail, vm.getClassroomsStatesMap["A"])
        assertFalse(vm.classroomDataMap.containsKey("A"))
    }

    @Test fun `time table read failure is reported without caching partial classroom results`() = runTest(dispatcher) {
        coEvery { repo.getClassroomInfos("A") } returns listOf(ClassroomInfo("A101", "1"))
        coEvery { schedule.timeTable.get() } throws IOException("settings unavailable")
        vm.loadClassroomInfos("A")?.join()
        assertEquals(SimpleState.Fail, vm.getClassroomsStatesMap["A"])
        assertFalse(vm.classroomDataMap.containsKey("A"))
        coEvery { schedule.timeTable.get() } returns morning
        vm.loadClassroomInfos("A")?.join()
        assertEquals(SimpleState.Success, vm.getClassroomsStatesMap["A"])
        coVerify(exactly = 2) { repo.getClassroomInfos("A") }
    }

    @Test fun `cancelled classroom load clears loading without reporting failure`() = runTest(dispatcher) {
        coEvery { repo.getClassroomInfos("A") } coAnswers { awaitCancellation() }
        val job = vm.loadClassroomInfos("A")!!
        runCurrent()
        job.cancelAndJoin()
        assertNull(vm.getClassroomsStatesMap["A"])
        assertNull(vm.getClassroomLastStatusLiveData.value)
        assertFalse(vm.classroomDataMap.containsKey("A"))
    }

    @Test fun `new building load cancels the old request without overwriting its state`() = runTest(dispatcher) {
        coEvery { login.withSchoolSession<List<BuildingInfo>>(any()) } coAnswers {
            firstArg<suspend () -> List<BuildingInfo>>().invoke()
        }
        val started = CompletableDeferred<Unit>()
        var requests = 0
        coEvery { repo.getBuildingInfos("01") } coAnswers {
            if (requests++ == 0) { started.complete(Unit); awaitCancellation() }
            emptyList()
        }
        val old = vm.loadBuildingTypes()
        started.await()
        vm.loadBuildingTypes().join()
        old.join()
        assertTrue(old.isCancelled)
        assertTrue(vm.getBuildingTypeStatusLiveData.value is SimpleDataState.Success)
    }

    private val morning = listOf(
        TimeTableItem(LocalTime.of(8, 0), LocalTime.of(8, 45)),
        TimeTableItem(LocalTime.of(8, 50), LocalTime.of(9, 35)),
        TimeTableItem(LocalTime.of(9, 55), LocalTime.of(10, 40)),
    )

    @Test fun `current occupied section is busy even when no later sections are occupied`() {
        val now = LocalTime.of(8, 10)
        val room = vm.calculateAvailability(ClassroomInfo("A101", "1"), morning, now, 5)
        assertFalse(vm.isFreeNow(room, now, 5))
        assertEquals(LocalTime.MIN, room.nextBusyTime)
        assertEquals(LocalTime.of(8, 45), room.nextFreeTime)
        assertEquals("2~3", room.prettyFreeTimes)
    }

    @Test fun `short breaks are joined but a break equal to threshold is usable`() {
        val room = ClassroomInfo("A101", "1,2,3")
        assertEquals(LocalTime.of(9, 35), vm.calculateAvailability(room, morning, LocalTime.of(8, 10), 10).nextFreeTime)
        assertEquals(LocalTime.of(8, 45), vm.calculateAvailability(room, morning, LocalTime.of(8, 10), 5).nextFreeTime)
    }

    @Test fun `before class exact end and after last class have consistent availability`() {
        val occupied = ClassroomInfo("A101", "1,3")
        assertEquals(LocalTime.of(8, 0), vm.calculateAvailability(occupied, morning, LocalTime.of(7, 50), 5).nextBusyTime)
        val breakRoom = vm.calculateAvailability(occupied, morning, LocalTime.of(8, 45), 5)
        assertEquals(LocalTime.of(9, 55), breakRoom.nextBusyTime)
        assertNull(breakRoom.nextFreeTime)
        val last = vm.calculateAvailability(occupied, morning, LocalTime.of(10, 0), 5)
        assertEquals(LocalTime.of(10, 40), last.nextFreeTime)
        assertEquals(LocalTime.MAX, vm.calculateAvailability(occupied, morning, LocalTime.of(10, 40), 5).nextBusyTime)
    }

}
