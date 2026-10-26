package cn.bit101.android.data.repo

import androidx.room.withTransaction
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.config.setting.base.LexueSubscription
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.database.BIT101Database
import cn.bit101.android.data.database.dao.DDLScheduleDao
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

internal class DefaultDDLScheduleRepo @Inject constructor(
    private val database: BIT101Database,
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus,
    private val smsCodeRequestHub: SmsCodeRequestHub,
    private val settings: DDLSettings,
    private val account: AccountSession,
) : DDLScheduleRepo {
    override val calendarUrl = combine(settings.subscription, loginStatus.sid.flow) { subscription, sid ->
        subscription.url.takeIf { sid.isNotBlank() && subscription.ownerSid == sid }.orEmpty()
    }.distinctUntilChanged()

    override suspend fun refreshSubscription() = withContext(Dispatchers.IO) {
        val owner = account.identity()
        require(owner.sid.isNotBlank()) { "请先登录校园账号" }
        val (password, webVpn) = account.commit(owner) { loginStatus.password.get() to loginStatus.webVpn.get() }
        val url = logApiCall(TAG, "获取乐学日历订阅链接", result = { "成功" }) {
            apiManager.api.schoolLexue.getCalendarUrl(
                username = owner.sid, password = password, webVpn = webVpn,
                smsCodeHandler = smsCodeRequestHub.createSmsCodeHandler(),
            )
        }
        account.commit(owner) { settings.setSubscription(LexueSubscription(owner.sid, url)) }
    }

    override suspend fun syncCalendar() = withContext(Dispatchers.IO) {
        val owner = account.identity()
        val (url, cookies, webVpn) = account.commit(owner) {
            val subscription = settings.subscription.first()
            require(owner.sid.isNotBlank() && subscription.ownerSid == owner.sid && subscription.url.isNotBlank()) {
                "请先获取当前账号的订阅链接"
            }
            Triple(subscription.url, SchoolCookieStore.snapshot(loginStatus.cookieManager.cookieStore), loginStatus.webVpn.get())
        }
        val events = logApiCall(TAG, "获取乐学日历", result = { "成功, 共 ${it.size} 个事件" }) {
            apiManager.api.schoolLexue.getCalendar(cookies = cookies, webVpn = webVpn, url = url)
        }.distinctBy { it.uid }
        // 网络期间允许切号，提交时重新核对账号并与退出、切号互斥。
        account.commit(owner) {
            database.withTransaction {
                val dao = database.DDLScheduleDao()
                val existing = dao.getUIDs(events.map { it.uid }, owner.sid).associateBy { it.uid }
                for (event in events) {
                    val previous = existing[event.uid]
                    val item = DDLScheduleEntity(
                        id = previous?.id ?: 0, uid = event.uid, group = "lexue", ownerSid = owner.sid,
                        title = event.event, text = event.course + "\n\n" + event.description,
                        time = event.time, done = previous?.done ?: false,
                    )
                    if (previous == null) dao.insert(item) else dao.update(item)
                }
            }
        }
    }

    override suspend fun insertDDL(ddl: DDLScheduleEntity) = withContext(Dispatchers.IO) {
        require(ddl.group != "lexue" && ddl.ownerSid.isBlank())
        database.DDLScheduleDao().insert(ddl)
    }

    override suspend fun updateDDL(ddl: DDLScheduleEntity) = change(ddl) { it.update(ddl) }

    override suspend fun setDone(ddl: DDLScheduleEntity, done: Boolean) = change(ddl) {
        it.setDone(ddl.id, ddl.ownerSid, done)
    }

    override suspend fun deleteDDL(ddl: DDLScheduleEntity) = change(ddl) { it.delete(ddl) }

    private suspend fun change(ddl: DDLScheduleEntity, operation: suspend (DDLScheduleDao) -> Unit) = withContext(Dispatchers.IO) {
        if (ddl.group == "lexue") {
            val owner = account.identity()
            if (owner.sid.isBlank() || ddl.ownerSid != owner.sid) throw CancellationException("账号已切换")
            account.commit(owner) { operation(database.DDLScheduleDao()) }
        } else {
            operation(database.DDLScheduleDao())
        }
    }

    override fun getFutureDDL(time: LocalDateTime) =
        combine(database.DDLScheduleDao().getFuture(time), loginStatus.sid.flow) { events, sid ->
            events.filter { it.group != "lexue" || sid.isNotBlank() && it.ownerSid == sid }
        }

    private companion object {
        const val TAG = "DDLScheduleRepo"
    }
}
