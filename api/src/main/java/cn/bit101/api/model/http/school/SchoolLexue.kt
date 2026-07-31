package cn.bit101.api.model.http.school

import java.time.LocalDateTime

class GetCalendarDataModel {
    data class CalendarEvent(
        val uid: String,
        val event: String,
        val description: String,
        val course: String,
        val time: LocalDateTime
    )
}
