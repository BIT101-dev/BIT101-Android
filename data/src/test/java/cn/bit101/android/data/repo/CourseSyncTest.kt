package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.api.service.school.SchoolResponseError
import cn.bit101.api.service.school.SchoolSessionExpired
import cn.bit101.bitlogin.sso.SsoHttpException
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CourseSyncTest {
    private val courses = mockk<CoursesRepo>(relaxed = true)
    private val login = mockk<SchoolLoginRepo>()
    private val settings = mockk<CourseScheduleSettings>(relaxed = true)
    private val status = MemoryLoginStatus()
    private val account = AccountSession(status, courses)
    private val sync = CourseSync(courses, login, settings, account)
    private val oldTerm = MemorySettingItem("old")
    private val firstDay = LocalDate.of(2026, 9, 7)
    private val records = listOf(mockk<CourseScheduleEntity>())

    @Before fun setUp() = runBlocking<Unit> {
        status.seedSchool()
        every { settings.term } returns oldTerm
        coEvery { courses.saveSchedule(any(), any(), any(), any()) } coAnswers {
            arg<String?>(2)?.let { oldTerm.set(it) }
        }
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers { firstArg<suspend () -> Any>().invoke() }
        coEvery { courses.getCurrentTermFromNet() } returns "new"
        coEvery { courses.getFirstDayFromNet(any()) } returns firstDay
        coEvery { courses.getCoursesFromNet(any()) } returns records
        coEvery { courses.getExamsFromNet(any()) } returns emptyList()
    }

    @Test fun `exam failure commits courses and term while preserving exam cache`() = runBlocking {
        val failure = SchoolResponseError("HTTP 503")
        coEvery { courses.getExamsFromNet("new") } throws failure
        assertSame(failure, sync.refresh().examFailure)
        coVerify(exactly = 1) { courses.saveSchedule(records, null, "new", firstDay) }
    }

    @Test fun `term switch failure preserves original cache without network rollback`() = runBlocking {
        coEvery { courses.getCoursesFromNet("new") } throws SchoolResponseError("HTTP 503")
        val error = assertThrows(CourseSyncException::class.java) { runBlocking { sync.refresh("new") } }
        assertEquals("获取课程表", error.step)
        assertEquals("old", oldTerm.get())
        coVerify(exactly = 0) { courses.saveSchedule(any(), any(), any(), any()); settings.setTerm(any(), any()); courses.getFirstDayFromNet("old") }
    }

    @Test fun `session failure reaches recovery before step is attached`() = runBlocking {
        coEvery { courses.getCoursesFromNet("new") } throws SchoolSessionExpired() andThen records
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers {
            val operation = firstArg<suspend () -> Any>()
            try { operation() } catch (e: SchoolSessionExpired) { operation() }
        }
        assertNull(sync.refresh().examFailure)
        coVerify(exactly = 2) { courses.getCoursesFromNet("new") }
        coVerify(exactly = 1) { courses.saveSchedule(records, emptyList(), "new", firstDay) }
    }

    @Test fun `expired exam session can recover independently of fetched courses`() = runBlocking {
        coEvery { courses.getExamsFromNet("new") } throws SchoolSessionExpired() andThen emptyList()
        coEvery { login.withSchoolSession<Any>(any()) } coAnswers {
            val operation = firstArg<suspend () -> Any>()
            try { operation() } catch (e: SchoolSessionExpired) { operation() }
        }
        assertNull(sync.refresh().examFailure)
        coVerify(exactly = 1) { courses.getCoursesFromNet("new") }
        coVerify(exactly = 2) { courses.getExamsFromNet("new") }
    }

    @Test fun `cancelled exam fetch never commits a partial sync`() = runBlocking {
        coEvery { courses.getExamsFromNet(any()) } throws CancellationException()
        assertThrows(CancellationException::class.java) { runBlocking { sync.refresh() } }
        coVerify(exactly = 0) { courses.saveSchedule(any(), any(), any(), any()); settings.setTerm(any(), any()) }
    }

    @Test fun `account switch after fetch rejects old writes and clears school cache`() = runBlocking {
        coEvery { courses.getExamsFromNet(any()) } coAnswers {
            account.commit(account.identity(), "another-student") { }
            emptyList()
        }
        assertThrows(CancellationException::class.java) { runBlocking { sync.refresh() } }
        coVerify(exactly = 0) { courses.saveSchedule(records, any(), any(), any()) }
        coVerify(exactly = 1) { courses.saveSchedule(emptyList(), emptyList(), "", null) }
    }

    @Test fun `logout during network request prevents stale commit even if same student logs back in`() = runBlocking {
        coEvery { courses.getExamsFromNet(any()) } coAnswers {
            account.logout()
            account.commit(account.identity(), "112233") { }
            emptyList()
        }
        assertThrows(CancellationException::class.java) { runBlocking { sync.refresh() } }
        coVerify(exactly = 0) { courses.saveSchedule(records, any(), any(), any()) }
    }

    @Test fun `home and settings serialize updates and each request uses its own term`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val resume = CompletableDeferred<Unit>()
        coEvery { courses.getCoursesFromNet("new") } coAnswers { started.complete(Unit); resume.await(); records }
        val home = async { sync.refresh() }
        started.await()
        val settingsRequest = async { sync.refresh("selected") }
        yield()
        coVerify(exactly = 0) { courses.getFirstDayFromNet("selected") }
        resume.complete(Unit)
        home.await()
        settingsRequest.await()
        assertEquals("selected", oldTerm.get())
    }

    @Test fun `failure log excludes request credentials`() = runBlocking<Unit> {
        mockkStatic(Log::class)
        try {
            every { Log.e(any(), any()) } returns 0
            coEvery { login.withSchoolSession<Any>(any()) } throws SsoHttpException(
                503, "https://sso.bit.edu.cn/cas/login?ticket=secret", "secret",
            )

            assertThrows(CourseSyncException::class.java) { runBlocking { sync.refresh() } }
            verify(exactly = 1) {
                Log.e("CourseSync", "获取当前学期 失败: cn.bit101.bitlogin.sso.SsoHttpException")
            }
        } finally {
            unmockkStatic(Log::class)
        }
    }
}
