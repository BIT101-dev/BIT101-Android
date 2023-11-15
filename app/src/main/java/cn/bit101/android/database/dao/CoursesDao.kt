package cn.bit101.android.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cn.bit101.android.database.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoursesDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE term = :term")
    fun getCoursesByTerm(term: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE term = :term AND weeks LIKE '%[' || :week || ']%'")
    fun getCoursesByTermWeek(term: String, week: Int): Flow<List<CourseEntity>>

    @Insert
    suspend fun insertCourse(course: CourseEntity)

    @Insert
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()

    @Query("DELETE FROM courses WHERE term = :term")
    suspend fun deleteCoursesByTerm(term: String)

}