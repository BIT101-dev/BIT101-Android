package cn.bit101.android.data.repo.base

import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CoursesRepo {

    /**
     * 从网络获取学期的第一天
     */
    suspend fun getFirstDayFromNet(term: String): LocalDate

    /**
     * 从本地获取学期的第一天
     */
    fun getFirstDayFromLocal(): Flow<LocalDate?>

    /**
     * 从网络获取学期列表，这个是不需要存储到本地的
     */
    suspend fun getTermListFromNet(): List<String>

    /**
     * 从网络中获取当前学期
     */
    suspend fun getCurrentTermFromNet(): String

    /**
     * 从本地获取当前学期
     */
    fun getCurrentTermFromLocal(): Flow<String?>

    /**
     * 根据学期、星期从本地获得课表
     */
    fun getCoursesFromLocal(term: String, week: Int): Flow<List<CourseScheduleEntity>>

    /**
     * 从本地获得所有课表
     */
    fun getCoursesFromLocal(): Flow<List<CourseScheduleEntity>>

    /**
     * 根据学期从本地获得课表
     */
    fun getCoursesFromLocal(term: String): Flow<List<CourseScheduleEntity>>

    /**
     * 从网络中获取学期对应的课表
     */
    suspend fun getCoursesFromNet(term: String): List<CourseScheduleEntity>

    /**
     * 更新数据库中某学期的课表
     */
    suspend fun saveCourses(courses: List<CourseScheduleEntity>)

    /**
     * 从本地获得所有考试安排
     */
    fun getExamsFromLocal(): Flow<List<ExamScheduleEntity>>

    /**
     * 根据学期从本地获得考试安排
     */
    fun getExamsFromLocal(term: String): Flow<List<ExamScheduleEntity>>

    /**
     * 从网络中获取学期对应的考试安排
     */
    suspend fun getExamsFromNet(term: String): List<ExamScheduleEntity>

    /**
     * 更新数据库中某学期的考试安排
     */
    suspend fun saveExams(exams: List<ExamScheduleEntity>)
}