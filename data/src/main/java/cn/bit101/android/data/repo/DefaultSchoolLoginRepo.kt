package cn.bit101.android.data.repo

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.*
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

    override suspend fun login(username: String, password: String, smsCodeHandler: SmsCodeHandler?) =
        withContext(Dispatchers.IO) {
            val identity = account.identity()
            mutex.withLock { authenticate(identity, username.trim(), password, smsCodeHandler) }
            true
        }

    override suspend fun reconnect() = withContext(Dispatchers.IO) {
        val identity = account.identity()
        mutex.withLock { restore(identity) }
        true
    }

    private suspend fun authenticate(identity: AccountSession.Identity, sid: String, password: String, handler: SmsCodeHandler?) {
        try {
            require(sid.isNotBlank() && password.isNotBlank()) { "请填写学号和密码" }
            val school = apiManager.api.schoolLogin.login(sid, password, handler)
            SchoolCookieStore.replace(CookieManager().cookieStore, school.cookies)
            currentCoroutineContext().ensureActive()
            account.commit(identity, sid) {
                SchoolCookieStore.replace(loginStatus.cookieManager.cookieStore, school.cookies)
                apiManager.switch(school.webVpn)
                if (loginStatus.password.get() != password && loginStatus.communityPasswordDigest.get().isBlank()) {
                    loginStatus.communityRetryAt.set(0)
                    loginStatus.communityAttempts.set(0)
                    loginStatus.communityNeedsLogin.set(false)
                }
                loginStatus.password.set(password)
                loginStatus.schoolConnected.set(true)
                sessionVersion++
            }
        } catch (e: Exception) {
            throw e.asLoginException("校园登录")
        }
    }

    private suspend fun restore(identity: AccountSession.Identity) {
        try {
            authenticate(identity, identity.sid, loginStatus.password.get(), smsCodeRequestHub.createSmsCodeHandler())
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
        return try {
            operation()
        } catch (e: Exception) {
            // SDK 的教务错误没有结构化状态码，只识别其明确的 HTTP 401。
            val expired = (e is SsoHttpException && e.statusCode == 401) ||
                (e is JxzxehallDataError && e.message?.endsWith("HTTP 401") == true)
            if (!expired) throw e
            mutex.withLock {
                if (identity != account.identity()) throw CancellationException("账号已切换")
                if (sessionVersion == version) restore(identity)
            }
            operation()
        }
    }
}
