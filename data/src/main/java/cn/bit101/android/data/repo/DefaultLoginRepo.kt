package cn.bit101.android.data.repo

import android.database.sqlite.SQLiteException
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.AESUtils
import cn.bit101.android.data.common.HashUtils
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.LoginRefreshResult
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.api.model.http.bit101.PostRegisterDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyInitDataModel
import cn.bit101.bitlogin.sso.CaptchaError
import cn.bit101.bitlogin.sso.LoginError as SsoLoginError
import cn.bit101.bitlogin.sso.SmsVerificationError
import cn.bit101.bitlogin.sso.SsoHttpException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject


internal class DefaultLoginRepo @Inject constructor(
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus,
    private val smsCodeRequestHub: SmsCodeRequestHub,
) : LoginRepo {

    private val api
        get() = apiManager.api

    private val loginMutex = Mutex()

    // 纯校验：学校会话是否有效，不再承担自动重登职责
    private suspend fun checkSchoolLogin() = withContext(Dispatchers.IO) {
        try {
            logApiCall(TAG, "校验学校登录态", result = { "成功, 当前学期: $it" }) {
                api.schoolJxzxehall.getCurrentTerm(
                    cookies = SchoolCookieStore.snapshot(loginStatus.cookieManager.cookieStore),
                    webVpn = loginStatus.webVpn.get(),
                )
            }.isNotBlank()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun checkBit101Login() = withContext(Dispatchers.IO) {
        api.user.check().isSuccessful
    }

    override suspend fun checkLogin() = withContext(Dispatchers.IO) {
        loginMutex.withLock {
            val valid = try {
                checkBit101Login() && checkSchoolLogin()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }

            if (valid) {
                return@withLock true
            }

            when (refreshLoginLocked(allowInteractive = true)) {
                LoginRefreshResult.SUCCESS -> true
                LoginRefreshResult.NEEDS_INTERACTIVE -> {
                    loginStatus.status.set(false)
                    false
                }
                LoginRefreshResult.TRANSIENT -> false
                LoginRefreshResult.FAILED -> {
                    withContext(NonCancellable) { loginStatus.clear() }
                    false
                }
                // checkLogin 已持有 loginMutex，refreshLoginLocked 不可能返回 BUSY，保守按不清理处理
                LoginRefreshResult.BUSY -> false
            }
        }
    }

    override suspend fun refreshLogin(allowInteractive: Boolean): LoginRefreshResult =
        withContext(Dispatchers.IO) {
            loginMutex.withLock {
                refreshLoginLocked(allowInteractive)
            }
        }

    // 非阻塞式刷新：互斥锁空闲时刷新，被持锁流程（checkLogin/login/refreshLogin）占用时立即返回 BUSY，
    // 供 OkHttp 401 拦截器调用，避免在持锁流程内重入同一把非重入锁而死锁
    override suspend fun tryRefreshLogin(): LoginRefreshResult =
        withContext(Dispatchers.IO) {
            if (loginMutex.tryLock()) {
                try {
                    refreshLoginLocked(allowInteractive = false)
                } finally {
                    loginMutex.unlock()
                }
            } else {
                LoginRefreshResult.BUSY
            }
        }

    // 调用方需持有 loginMutex，避免 checkLogin 中重复加锁导致死锁
    private suspend fun refreshLoginLocked(allowInteractive: Boolean): LoginRefreshResult {
        val sid = loginStatus.sid.get()
        val password = loginStatus.password.get()
        if (sid.isEmpty() || password.isEmpty()) {
            return LoginRefreshResult.FAILED
        }

        val smsCodeHandler = if (allowInteractive) smsCodeRequestHub.createSmsCodeHandler() else null

        return try {
            loginSchool(sid, password, smsCodeHandler)
            loginBIT101(sid, password)
            LoginRefreshResult.SUCCESS
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.classifyRefreshFailure()
        }
    }

    // 依据 BIT-Login SDK 异常类型对刷新失败分类，遍历 cause 链以便穿透 SDK 的包装
    private fun Throwable.classifyRefreshFailure(): LoginRefreshResult {
        var current: Throwable? = this
        val visited = mutableSetOf<Throwable>()
        while (current != null && visited.add(current)) {
            when (current) {
                is CancellationException, is SmsVerificationError, is CaptchaError -> {
                    return LoginRefreshResult.NEEDS_INTERACTIVE
                }
                is SsoHttpException -> {
                    if (current.statusCode == 429 || current.statusCode in 500..599) {
                        return LoginRefreshResult.TRANSIENT
                    }
                    return LoginRefreshResult.FAILED
                }
                is SsoLoginError -> {
                    if (current.statusCode == 429 || current.statusCode in 500..599) {
                        return LoginRefreshResult.TRANSIENT
                    }
                    return LoginRefreshResult.FAILED
                }
                is IOException -> return LoginRefreshResult.TRANSIENT
            }
            current = current.cause
        }
        return LoginRefreshResult.FAILED
    }

    private suspend fun loginSchool(
        username: String,
        password: String,
        smsCodeHandler: SmsCodeHandler? = null,
    ) = withContext(Dispatchers.IO) {
        val result = logApiCall(TAG, "学校统一身份认证登录(sid=$username)", result = { "成功, webVpn=${it.webVpn}" }) {
            api.schoolLogin.login(username, password, smsCodeHandler)
        }
        SchoolCookieStore.replace(loginStatus.cookieManager.cookieStore, result.cookies)
        apiManager.switch(result.webVpn)
        true
    }

    private suspend fun loginBIT101(username: String, password: String) = withContext(Dispatchers.IO) {
        val initData = api.user.webVpnVerifyInit(
            PostWebvpnVerifyInitDataModel.Body(
                sid = username,
            )
        ).body() ?: throw Exception("init bit101 login error")

        val salt = initData.salt
        val execution = initData.execution
        val cookie = initData.cookie
        val encryptedPassword = AESUtils.encryptPassword(password, salt)

        val verifyData = api.user.webVpnVerify(
            PostWebvpnVerifyDataModel.Body(
                sid = username,
                password = encryptedPassword,
                execution = execution,
                cookie = cookie,
                salt = salt,
                captcha = ""
            )
        ).body() ?: throw Exception("get webVpnVerify response error")
        val md5Password = HashUtils.md5(password)
        val token = verifyData.token
        val code = verifyData.code

        val fakeCookie = api.user.register(
            PostRegisterDataModel.Body(
                password = md5Password,
                token = token,
                code = code,
                loginMode = true
            )
        ).body()?.fakeCookie ?: throw Exception("get register response error")

        loginStatus.fakeCookie.set(fakeCookie)

        true
    }

    override suspend fun login(
        username: String,
        password: String,
        smsCodeHandler: SmsCodeHandler?,
    ) = withContext(Dispatchers.IO) {
        loginMutex.withLock {
            loginStatus.clear()
            try {
                loginSchool(username, password, smsCodeHandler)
                loginBIT101(username, password)
                loginStatus.sid.set(username)
                loginStatus.password.set(password)
                loginStatus.status.set(true)
                true
            } catch (e: Exception) {
                withContext(NonCancellable) { loginStatus.clear() }
                throw e
            }
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        loginMutex.withLock {
            withContext(NonCancellable) { loginStatus.clear() }
        }
    }

    override suspend fun <T> doOperationRequiresLogin(operation: suspend () -> T): T {
        return try {
            operation()
        } catch (e: Exception) {
            if (e is CancellationException || e is IllegalArgumentException || e is SQLiteException) {
                throw e
            }

            val checkLoginSuccess = try {
                checkLogin()
            } catch (checkError: Exception) {
                if (checkError is CancellationException) throw checkError
                throw IllegalStateException("check login failed", e).apply {
                    addSuppressed(checkError)
                }
            }

            if (checkLoginSuccess) {
                operation()
            } else {
                throw IllegalStateException("check login failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "LoginRepo"
    }
}
