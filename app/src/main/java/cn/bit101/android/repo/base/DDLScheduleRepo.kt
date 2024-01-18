package cn.bit101.android.repo.base

import cn.bit101.android.database.entity.DDLScheduleEntity
import cn.bit101.api.model.http.school.GetCalendarDataModel

interface DDLScheduleRepo {
    suspend fun getCalendarUrl(): String?

    suspend fun getCalendarFromNet(url: String): List<GetCalendarDataModel.CalendarEvent>

    suspend fun getCalendarFromLocal(uids: List<String>): List<DDLScheduleEntity>

    suspend fun insertDDL(ddl: DDLScheduleEntity)

    suspend fun updateDDL(ddl: DDLScheduleEntity)
}