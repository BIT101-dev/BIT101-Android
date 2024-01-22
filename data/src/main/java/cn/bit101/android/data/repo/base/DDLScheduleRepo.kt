package cn.bit101.android.data.repo.base

import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.api.model.http.school.GetCalendarDataModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface DDLScheduleRepo {
    suspend fun getCalendarUrl(): String?

    suspend fun getCalendarFromNet(url: String): List<GetCalendarDataModel.CalendarEvent>

    suspend fun getCalendarFromLocal(uids: List<String>): List<DDLScheduleEntity>

    suspend fun insertDDL(ddl: DDLScheduleEntity)

    suspend fun updateDDL(ddl: DDLScheduleEntity)

    suspend fun deleteDDL(ddl: DDLScheduleEntity)

    fun getFutureDDL(time: LocalDateTime): Flow<List<DDLScheduleEntity>>

    suspend fun getDDLByUIDs(
        uids: List<String>
    ): List<DDLScheduleEntity>
}