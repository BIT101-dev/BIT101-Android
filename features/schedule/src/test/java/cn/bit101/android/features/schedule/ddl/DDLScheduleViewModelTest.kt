package cn.bit101.android.features.schedule.ddl

import androidx.lifecycle.ViewModelStore
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class DDLScheduleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private lateinit var repo: DDLScheduleRepo
    private lateinit var settings: DDLSettings
    private lateinit var vm: DDLScheduleViewModel
    private val event = DDLScheduleEntity(1, "main", "fixture", "作业", "", LocalDateTime.now(), false)

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk(relaxed = true)
        settings = mockk()
        every { repo.calendarUrl } returns flowOf("")
        every { settings.beforeDay.flow } returns flowOf(7L)
        every { settings.afterDay.flow } returns flowOf(3L)
        every { repo.getFutureDDL(any()) } returns flowOf(listOf(event))
        val login = mockk<SchoolLoginRepo>()
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        vm = DDLScheduleViewModel(repo, settings, login)
        store.put("ddl", vm)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun `database read failure stays on page and retry recovers`() = runTest(dispatcher) {
        every { repo.getFutureDDL(any()) } returns flow { throw IOException("database unavailable") }
        runCurrent()
        assertNotNull(vm.loadError.value)
        every { repo.getFutureDDL(any()) } returns flowOf(listOf(event))
        vm.loadEvents()
        runCurrent()
        assertNull(vm.loadError.value)
        assertEquals(listOf(event), vm.events.value)
    }

    @Test fun `all edit failures are reported without closing the dialog or claiming success`() = runTest(dispatcher) {
        runCurrent()
        coEvery { repo.insertDDL(any()) } throws IOException("disk full")
        coEvery { repo.updateDDL(any()) } throws IOException("disk full")
        coEvery { repo.deleteDDL(any()) } throws IOException("disk full")
        coEvery { repo.setDone(any(), any()) } throws IOException("disk full")
        var closed = false
        vm.addDDL("作业", event.time, "") { closed = true }?.join()
        assertTrue(vm.editState.value is SimpleErrorDataState.Fail)
        vm.updateDDL(event, "作业", event.time, "") { closed = true }?.join()
        assertTrue(vm.editState.value is SimpleErrorDataState.Fail)
        vm.deleteDDL(event) { closed = true }?.join()
        assertTrue(vm.editState.value is SimpleErrorDataState.Fail)
        vm.setDone(event, true)?.join()
        assertTrue(vm.editState.value is SimpleErrorDataState.Fail)
        assertFalse(closed)
    }

    @Test fun `success is only published after storage completes and repeated clicks share one write`() = runTest(dispatcher) {
        runCurrent()
        val saved = CompletableDeferred<Unit>()
        coEvery { repo.insertDDL(any()) } coAnswers { saved.await() }
        var closed = false
        val job = vm.addDDL("作业", event.time, "") { closed = true }
        repeat(5) { assertNull(vm.addDDL("作业", event.time, "")) }
        runCurrent()
        assertFalse(closed)
        saved.complete(Unit)
        job?.join()
        assertTrue(closed)
        assertTrue(vm.editState.value is SimpleErrorDataState.Success)
        coVerify(exactly = 1) { repo.insertDDL(any()) }
    }

    @Test fun `subscription failure prevents download and cancellation is not shown as failure`() = runTest(dispatcher) {
        runCurrent()
        coEvery { repo.refreshSubscription() } throws IOException("offline")
        vm.refreshCalendar(acquireUrl = true)?.join()
        assertTrue(vm.refreshState.value is SimpleErrorDataState.Fail)
        coVerify(exactly = 0) { repo.syncCalendar() }
        coEvery { repo.refreshSubscription() } coAnswers { awaitCancellation() }
        val job = vm.refreshCalendar(acquireUrl = true)!!
        runCurrent()
        job.cancelAndJoin()
        assertNull(vm.refreshState.value)
        coVerify(exactly = 0) { repo.syncCalendar() }
    }

    @Test fun `calendar download failure preserves the displayed local events`() = runTest(dispatcher) {
        runCurrent()
        every { repo.calendarUrl } returns flowOf("https://lexue.bit.edu.cn/calendar/export_execute.php")
        coEvery { repo.syncCalendar() } throws IOException("offline")
        vm.refreshCalendar()?.join()
        assertEquals(listOf(event), vm.events.value)
        assertTrue(vm.refreshState.value is SimpleErrorDataState.Fail)
    }

    @Test fun `checking different events while a write is pending queues both changes`() = runTest(dispatcher) {
        runCurrent()
        val firstSaved = CompletableDeferred<Unit>()
        val other = event.copy(id = 2, uid = "other")
        coEvery { repo.setDone(event, true) } coAnswers { firstSaved.await() }
        val first = vm.setDone(event, true)
        val second = vm.setDone(other, true)
        assertNotNull(second)
        runCurrent()
        coVerify(exactly = 0) { repo.setDone(other, true) }
        firstSaved.complete(Unit)
        first?.join()
        second?.join()
        coVerifyOrder { repo.setDone(event, true); repo.setDone(other, true) }
    }

    @Test fun `rapid toggles of the same event are saved in order`() = runTest(dispatcher) {
        runCurrent()
        val firstSaved = CompletableDeferred<Unit>()
        coEvery { repo.setDone(event, true) } coAnswers { firstSaved.await() }
        vm.setDone(event, true)
        val last = vm.setDone(event, false)
        firstSaved.complete(Unit)
        last?.join()
        coVerifyOrder { repo.setDone(event, true); repo.setDone(event, false) }
    }

    @Test fun `one failed edit does not discard the next queued edit`() = runTest(dispatcher) {
        runCurrent()
        coEvery { repo.setDone(event, true) } throws IOException("write failure")
        vm.setDone(event, true)
        val other = event.copy(id = 2)
        vm.setDone(other, true)?.join()
        coVerify(exactly = 1) { repo.setDone(other, true) }
        assertTrue(vm.editState.value is SimpleErrorDataState.Success)
    }

    @Test fun `retry restarts subscription observation and receives later URL changes`() = runTest(dispatcher) {
        every { repo.calendarUrl } returns flow { throw IOException("settings unavailable") }
        runCurrent()
        assertNull(vm.lexueCalendarUrlFlow.value)
        assertNotNull(vm.loadError.value)
        // 订阅读取失败不应终止本地日程观察。
        assertEquals(listOf(event), vm.events.value)
        val urls = MutableStateFlow("https://lexue.bit.edu.cn/first")
        every { repo.calendarUrl } returns urls
        vm.loadEvents()
        runCurrent()
        assertNull(vm.loadError.value)
        assertEquals(urls.value, vm.lexueCalendarUrlFlow.value)
        urls.value = "https://lexue.bit.edu.cn/updated"
        runCurrent()
        assertEquals(urls.value, vm.lexueCalendarUrlFlow.value)
    }
}
