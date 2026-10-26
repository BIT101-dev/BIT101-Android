package cn.bit101.android.data.repo

import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.data.database.BIT101Database
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import cn.bit101.android.data.net.base.APIManager
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SchoolCacheVisibilityTest {
    @Test fun `leftover school cache is hidden once credentials have been cleared`() = runBlocking<Unit> {
        val status = MemoryLoginStatus()
        status.seedSchool()
        val db = mockk<BIT101Database>(relaxed = true)
        val courses = listOf(mockk<CourseScheduleEntity>())
        val exams = listOf(mockk<ExamScheduleEntity>())
        every { db.coursesDao().getAllCourses() } returns MutableStateFlow(courses)
        every { db.examsDao().getAllExams() } returns MutableStateFlow(exams)
        val repo = DefaultCoursesRepo(db, mockk(), mockk(), status)
        assertEquals(courses, repo.getCoursesFromLocal().first())
        assertEquals(exams, repo.getExamsFromLocal().first())
        status.clear()
        assertTrue(repo.getCoursesFromLocal().first().isEmpty())
        assertTrue(repo.getExamsFromLocal().first().isEmpty())
    }

    @Test fun `classroom queries no longer use selected timetable term`() = runBlocking<Unit> {
        val status = MemoryLoginStatus()
        status.seedSchool()
        val manager = mockk<APIManager>(relaxed = true)
        val settings = mockk<FreeClassroomSettings>()
        val repo = DefaultFreeClassroomRepo(manager, settings, status)
        repo.getClassroomInfos("building")
        coVerify(exactly = 1) {
            manager.api.schoolClassroom.getClassroomOccupancy(any(), false, "building", any())
        }
    }
}
