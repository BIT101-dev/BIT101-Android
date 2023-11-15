package cn.bit101.android.repo.model

import cn.bit101.android.database.entity.CourseEntity
import java.time.LocalDate

data class CourseScheduleResponse(
    val firstDay: LocalDate,
    val courseList: List<CourseEntity>
)