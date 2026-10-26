package cn.bit101.android.features.setting.viewmodel

import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.data.repo.CourseSync
import cn.bit101.android.data.repo.CourseSyncResult
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.api.service.school.SchoolResponseError
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val sync = mockk<CourseSync>()
    private lateinit var vm: CalendarViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        vm = CalendarViewModel(mockk<CoursesRepo>(relaxed = true), mockk<SchoolLoginRepo>(), sync,
            mockk<CourseScheduleSettings>(relaxed = true))
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `term switch uses shared sync and displays partial success warning`() = runTest(dispatcher) {
        coEvery { sync.refresh("new") } returns CourseSyncResult(SchoolResponseError("HTTP 503"))
        vm.setCurrentTerm("new")?.join()
        val message = (vm.syncState.value as SimpleErrorDataState.Success).data
        assertTrue(message.contains("课程已更新"))
        assertTrue(message.contains("503"))
    }

    @Test fun `all setting sync actions share loading guard and cancellation resets it`() = runTest(dispatcher) {
        coEvery { sync.refresh("new") } coAnswers { awaitCancellation() }
        val job = vm.setCurrentTerm("new")!!
        runCurrent()
        assertNull(vm.getCourses())
        assertNull(vm.getExams())
        assertNull(vm.getFirstDay())
        job.cancelAndJoin()
        assertNull(vm.syncState.value)
    }

    @Test fun `failure shows actual reason without starting rollback`() = runTest(dispatcher) {
        coEvery { sync.refresh("new") } throws SchoolResponseError("HTTP 503")
        vm.setCurrentTerm("new")?.join()
        assertTrue((vm.syncState.value as SimpleErrorDataState.Fail).msg.contains("503"))
        coVerify(exactly = 1) { sync.refresh(any()) }
    }
}
