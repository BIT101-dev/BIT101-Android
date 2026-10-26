package cn.bit101.android.features.schedule.course

import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.data.repo.CourseSync
import cn.bit101.android.data.repo.CourseSyncException
import cn.bit101.android.data.repo.CourseSyncResult
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.api.service.school.SchoolAccessDenied
import cn.bit101.api.service.school.SchoolResponseError
import cn.bit101.api.service.school.SchoolSessionExpired
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CourseScheduleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: CoursesRepo
    private lateinit var settings: CourseScheduleSettings
    private lateinit var sync: CourseSync
    private lateinit var vm: CourseScheduleViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk(relaxed = true)
        sync = mockk()
        settings = mockk(relaxed = true)
        every { settings.firstDay.flow } returns emptyFlow()
        every { settings.term.flow } returns emptyFlow()
        every { settings.timeTable.flow } returns emptyFlow()
        every { settings.showSaturday.flow } returns emptyFlow()
        every { settings.showSunday.flow } returns emptyFlow()
        every { settings.showBorder.flow } returns emptyFlow()
        every { settings.highlightToday.flow } returns emptyFlow()
        every { settings.showDivider.flow } returns emptyFlow()
        every { settings.showCurrentTime.flow } returns emptyFlow()
        every { settings.showExamInfo.flow } returns emptyFlow()
        every { repo.getCoursesFromLocal() } returns emptyFlow()
        every { repo.getExamsFromLocal() } returns emptyFlow()
        every { repo.getCustomSchedules() } returns emptyFlow()
        coEvery { sync.refresh() } returns CourseSyncResult()
        vm = CourseScheduleViewModel(repo, settings, sync)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `sync error exposes step and reason in persistent UI state`() = runTest(dispatcher) {
        coEvery { sync.refresh() } throws CourseSyncException("获取当前学期", SchoolAccessDenied())
        vm.forceRefreshCourses()?.join()
        val error = vm.refreshState.value as SimpleErrorDataState.Fail
        assertTrue(error.msg.contains("获取当前学期"))
        assertTrue(error.msg.contains("无权访问"))
        runCurrent()
        assertEquals(error, vm.refreshState.value)
    }

    @Test fun `duplicate clicks share one refresh`() = runTest(dispatcher) {
        val result = CompletableDeferred<CourseSyncResult>()
        coEvery { sync.refresh() } coAnswers { result.await() }
        val job = vm.forceRefreshCourses()
        repeat(10) { assertNull(vm.forceRefreshCourses()) }
        result.complete(CourseSyncResult())
        job?.join()
        assertTrue(vm.refreshState.value is SimpleErrorDataState.Success)
        coVerify(exactly = 1) { sync.refresh() }
    }

    @Test fun `exam warning remains available in success result`() = runTest(dispatcher) {
        val failure = SchoolResponseError("HTTP 503")
        coEvery { sync.refresh() } returns CourseSyncResult(failure)
        vm.forceRefreshCourses()?.join()
        val result = vm.refreshState.value as SimpleErrorDataState.Success
        assertSame(failure, result.data.examFailure)
    }

    @Test fun `cancellation clears loading without displaying a failure`() = runTest(dispatcher) {
        coEvery { sync.refresh() } coAnswers { awaitCancellation() }
        val job = vm.forceRefreshCourses()!!
        runCurrent()
        job.cancelAndJoin()
        assertNull(vm.refreshState.value)
    }
}
