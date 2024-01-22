package cn.bit101.android.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoursesDao {
    @Query("SELECT * FROM course_schedule")
    fun getAllCourses(): Flow<List<CourseScheduleEntity>>

    @Query("SELECT * FROM course_schedule WHERE term = :term")
    fun getCoursesByTerm(term: String): Flow<List<CourseScheduleEntity>>

    @Query("SELECT * FROM course_schedule WHERE term = :term AND weeks LIKE '%[' || :week || ']%'")
    fun getCoursesByTermWeek(term: String, week: Int): Flow<List<CourseScheduleEntity>>

    @Insert
    suspend fun insertCourse(course: CourseScheduleEntity)

    @Insert
    suspend fun insertCourses(courses: List<CourseScheduleEntity>)

    @Delete
    suspend fun deleteCourse(course: CourseScheduleEntity)

    @Query("DELETE FROM course_schedule")
    suspend fun deleteAllCourses()

    @Query("DELETE FROM course_schedule WHERE term = :term")
    suspend fun deleteCoursesByTerm(term: String)

}