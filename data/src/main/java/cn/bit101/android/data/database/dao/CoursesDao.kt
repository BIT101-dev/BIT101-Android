package cn.bit101.android.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CoursesDao {
    /**
     * 获取所有课程
     */
    @Query("SELECT * FROM course_schedule")
    fun getAllCourses(): Flow<List<CourseScheduleEntity>>

    /**
     * 获取指定学期的课程
     */
    @Query("SELECT * FROM course_schedule WHERE term = :term")
    fun getCoursesByTerm(term: String): Flow<List<CourseScheduleEntity>>

    /**
     * 获取指定学期指定周的课程
     */
    @Query("SELECT * FROM course_schedule WHERE term = :term AND weeks LIKE '%[' || :week || ']%'")
    fun getCoursesByTermWeek(term: String, week: Int): Flow<List<CourseScheduleEntity>>

    /**
     * 插入课程
     */
    @Insert
    suspend fun insertCourse(course: CourseScheduleEntity)

    /**
     * 插入若干课程
     */
    @Insert
    suspend fun insertCourses(courses: List<CourseScheduleEntity>)

    /**
     * 删除课程
     */
    @Delete
    suspend fun deleteCourse(course: CourseScheduleEntity)

    /**
     * 删除所有课程
     */
    @Query("DELETE FROM course_schedule")
    suspend fun deleteAllCourses()

    /**
     * 删除指定学期的所有课程
     */
    @Query("DELETE FROM course_schedule WHERE term = :term")
    suspend fun deleteCoursesByTerm(term: String)

}