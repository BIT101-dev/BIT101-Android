package cn.bit101.android.repo.base

import cn.bit101.api.model.http.school.GetCalendarDataModel

interface DDLScheduleRepo {
    suspend fun getCalendarUrl(): String?
    suspend fun getCalendar(url: String): List<GetCalendarDataModel.CalendarEvent>
}