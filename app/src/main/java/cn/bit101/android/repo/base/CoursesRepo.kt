package cn.bit101.android.repo.base

import cn.bit101.android.database.entity.CourseEntity
import cn.bit101.api.model.http.school.PostGetWeekAndDateDataModel
import java.time.LocalDate

interface CoursesRepo {
    suspend fun getFirstDayFromLocal(
        term: String
    ): LocalDate

    suspend fun getFirstDayFromNet(
        term: String,
        save: Boolean = true
    ): LocalDate

    suspend fun getTermListFromLocal(): List<String>

    suspend fun getTermListFromNet(
        save: Boolean = true
    ): List<String>

    suspend fun getCoursesFromLocal(
        term: String,
        week: Int
    ): List<CourseEntity>
    suspend fun getCoursesFromLocal(
        term: String
    ): List<CourseEntity>

    suspend fun getCoursesFromLocal(): List<CourseEntity>

    suspend fun getCoursesFromNet(
        term: String,
        save: Boolean = true,
    ): List<CourseEntity>

    suspend fun getCoursesFromNet(
        save: Boolean = true,
    ): List<CourseEntity>
}