package cn.bit101.android.data.repo

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.*
import cn.bit101.api.service.school.SchoolSessionExpired
import cn.bit101.api.service.school.SchoolRouteUnavailable
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.bitlogin.sso.SsoHttpException
import cn.bit101.bitlogin.api.jxzxehall.JxzxehallDataError
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.CookieManager
import javax.inject.Inject

internal class DefaultSchoolLoginRepo @Inject constructor(
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus,
    private val smsCodeRequestHub: SmsCodeRequestHub,
    private val account: AccountSession,
) : SchoolLoginRepo {
    private val mutex = Mutex()
    @Volatile private var sessionVersion = 0L
    private var recoveryFailure: Exception? = null

    override suspend fun login(username: String, password: String, smsCodeHandler: SmsCodeHandler?) =
        withContext(Dispatchers.IO) {
            val identity = account.identity()
            mutex.withLock { authenticate(identity, username.trim(), password, smsCodeHandler) }
        }

    override suspend fun reconnect() = withContext(Dispatchers.IO) {
        val identity = account.identity()
        mutex.withLock { restore(identity) }
    }

    private suspend fun authenticate(identity: AccountSession.Identity, sid: String, password: String, handler: SmsCodeHandler?, webVpn: Boolean? = null): SchoolConnectionResult {
        try {
            require(sid.isNotBlank() && password.isNotBlank()) { "请填写学号和密码" }
            val school = apiManager.api.schoolLogin.login(sid, password, handler, webVpn)
            SchoolCookieStore.replace(CookieManager().cookieStore, school.cookies, allowEmpty = school.teachingCenterWarning != null)
            currentCoroutineContext().ensureActive()
            account.commit(identity, sid) {
                SchoolCookieStore.replace(loginStatus.cookieManager.cookieStore, school.cookies, allowEmpty = school.teachingCenterWarning != null)
                apiManager.switch(school.webVpn)
                if (loginStatus.password.get() != password && loginStatus.communityPasswordDigest.get().isBlank()) {
                    loginStatus.communityRetryAt.set(0)
                    loginStatus.communityAttempts.set(0)
                    loginStatus.communityNeedsLogin.set(false)
                }
                loginStatus.password.set(password)
                loginStatus.schoolConnected.set(true)
                recoveryFailure = null
                sessionVersion++
            }
            return SchoolConnectionResult(school.teachingCenterWarning)
        } catch (e: Exception) {
            throw e.asLoginException("校园登录")
        }
    }

    private suspend fun restore(identity: AccountSession.Identity, webVpn: Boolean? = null): SchoolConnectionResult {
        try {
            return authenticate(identity, identity.sid, loginStatus.password.get(), smsCodeRequestHub.createSmsCodeHandler(), webVpn)
        } catch (e: LoginException) {
            if (e.result == LoginFailure.FAILED || e.result == LoginFailure.NEEDS_INTERACTIVE) {
                account.commit(identity) { loginStatus.schoolConnected.set(false) }
            }
            throw e
        }
    }

    override suspend fun logout() = account.logout()

    override suspend fun <T> withSchoolSession(operation: suspend () -> T): T {
        val identity = account.identity()
        val version = sessionVersion
        val result = try {
            operation()
        } catch (e: Exception) {
            // 业务会话失效或明确的路线连接失败各只恢复一次，共用同一重试预算。
            val expired = e is SchoolSessionExpired || (e is SsoHttpException && e.statusCode == 401) ||
                (e is JxzxehallDataError && e.message?.endsWith("HTTP 401") == true)
            if (!expired && e !is SchoolRouteUnavailable) throw e
            mutex.withLock {
                if (identity != account.identity()) throw CancellationException("账号已切换")
                if (sessionVersion == version) {
                    try {
                        restore(identity, (e as? SchoolRouteUnavailable)?.let { !it.webVpn })
                    } catch (failure: Exception) {
                        if (failure !is CancellationException) {
                            recoveryFailure = failure
                            sessionVersion++
                        }
                        throw failure
                    }
                } else {
                    recoveryFailure?.let { throw it }
                }
            }
            operation()
        }
        if (identity != account.identity()) throw CancellationException("账号已切换")
        return result
    }
}
