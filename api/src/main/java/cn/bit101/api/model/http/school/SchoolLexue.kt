package cn.bit101.api.model.http.school

import java.time.LocalDateTime

class GetLexueIndexDataModel {
    data class Response(
        val html: String,
        val sesskey: String?,
    )
}

class GetCalendarUrlDataModel {
    data class Response(
        val html: String,
        val url: String?,
    )
}

class GetCalendarDataModel {
    data class CalendarEvent(
        val uid: String,
        val event: String,
        val description: String,
        val course: String,
        val time: LocalDateTime
    )

    data class Response(
        val html: String,
        val calenders: List<CalendarEvent>,
    )
}