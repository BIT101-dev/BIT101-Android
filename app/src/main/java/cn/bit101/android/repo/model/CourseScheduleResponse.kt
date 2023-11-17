package cn.bit101.android.repo.model

import cn.bit101.android.database.entity.CourseScheduleEntity
import java.time.LocalDate

data class CourseScheduleResponse(
    val firstDay: LocalDate,
    val courseList: List<CourseScheduleEntity>
)