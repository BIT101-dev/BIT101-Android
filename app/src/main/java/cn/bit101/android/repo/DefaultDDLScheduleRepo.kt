package cn.bit101.android.repo

import android.util.Log
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.DDLScheduleEntity
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.DDLScheduleRepo
import cn.bit101.api.model.http.school.GetCalendarDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultDDLScheduleRepo @Inject constructor(
    private val database: BIT101Database,
) : DDLScheduleRepo {
    override suspend fun getCalendarUrl(): String? {
        val url = try {
            withContext(Dispatchers.IO) {
                val getResponse = BIT101API.schoolLexue.get()
                val sesskey = getResponse.body()?.sesskey ?: throw Exception("get sesskey error")

                val response = BIT101API.schoolLexue.getCalendarUrl(sesskey)
                response.body()?.url ?: throw Exception("get url error")
            }
        } catch (e: Exception) {
            Log.e("Lexue", e.toString())
            return null
        }
        Log.i("Lexue", "getCalendarUrl: $url")
        return url
    }

    override suspend fun getCalendarFromNet(
        url: String
    ) = withContext(Dispatchers.IO) {
        val response = BIT101API.schoolLexue.getCalendar(url)
        response.body()?.calenders ?: throw Exception("get calendar error")
    }

    override suspend fun getCalendarFromLocal(
        uids: List<String>
    ) = withContext(Dispatchers.IO) {
        database.DDLScheduleDao().getUIDs(uids)
    }

    override suspend fun insertDDL(
        ddl: DDLScheduleEntity
    ) = withContext(Dispatchers.IO) {
        database.DDLScheduleDao().insert(ddl)
    }

    override suspend fun updateDDL(
        ddl: DDLScheduleEntity
    ) = withContext(Dispatchers.IO) {
        database.DDLScheduleDao().update(ddl)
    }
}