package cn.bit101.android.repo.base

import cn.bit101.android.database.entity.CourseEntity
import cn.bit101.api.model.http.school.PostGetWeekAndDateDataModel
import java.time.LocalDate

interface CoursesRepo {
    suspend fun getCourses(
        forceNet: Boolean = false,
        autoSave: Boolean = true,
    ): List<CourseEntity>
    suspend fun getCourses(
        term: String = "",
        forceNet: Boolean = false,
        autoSave: Boolean = true,
    ): List<CourseEntity>
    suspend fun getCourses(
        term: String,
        week: Int,
        forceNet: Boolean = false,
        autoSave: Boolean = true,
    ): List<CourseEntity>

    suspend fun getTermList(
        forceNet: Boolean = false,
        autoSave: Boolean = true,
    ): List<String>

    suspend fun getFirstDay(
        term: String,
        forceNet: Boolean = false,
        autoSave: Boolean = true,
    ): LocalDate

    suspend fun getWeekAndDay(
        term: String
    ): List<PostGetWeekAndDateDataModel.Data>
}