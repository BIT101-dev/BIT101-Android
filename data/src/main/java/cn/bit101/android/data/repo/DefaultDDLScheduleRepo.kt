package cn.bit101.android.data.repo

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.database.BIT101Database
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

internal class DefaultDDLScheduleRepo @Inject constructor(
    private val database: BIT101Database,
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus,
    private val smsCodeRequestHub: SmsCodeRequestHub,
) : DDLScheduleRepo {

    override suspend fun getCalendarUrl(): String? {
        val url = try {
            withContext(Dispatchers.IO) {
                logApiCall(TAG, "获取乐学日历订阅链接", result = { "成功: $it" }) {
                    apiManager.api.schoolLexue.getCalendarUrl(
                        username = loginStatus.sid.get(),
                        password = loginStatus.password.get(),
                        webVpn = loginStatus.webVpn.get(),
                        smsCodeHandler = smsCodeRequestHub.createSmsCodeHandler(),
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        return url
    }

    override suspend fun getCalendarFromNet(
        url: String
    ) = withContext(Dispatchers.IO) {
        logApiCall(TAG, "获取乐学日历", result = { "成功, 共 ${it.size} 个事件" }) {
            apiManager.api.schoolLexue.getCalendar(
                cookies = SchoolCookieStore.snapshot(loginStatus.cookieManager.cookieStore),
                webVpn = loginStatus.webVpn.get(),
                url = url,
            )
        }
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

    override suspend fun deleteDDL(
        ddl: DDLScheduleEntity
    ) = withContext(Dispatchers.IO) {
        database.DDLScheduleDao().delete(ddl)
    }

    override fun getFutureDDL(
        time: LocalDateTime
    ) = database.DDLScheduleDao().getFuture(time)

    override suspend fun getDDLByUIDs(
        uids: List<String>
    ) = withContext(Dispatchers.IO) {
        database.DDLScheduleDao().getUIDs(uids)
    }

    private companion object {
        const val TAG = "DDLScheduleRepo"
    }

}
