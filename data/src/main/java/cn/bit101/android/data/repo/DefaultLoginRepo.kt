package cn.bit101.android.data.repo

import android.database.sqlite.SQLiteException
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.AESUtils
import cn.bit101.android.data.common.HashUtils
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.asLoginException
import cn.bit101.android.data.repo.base.invalidLoginResponse
import cn.bit101.android.data.repo.base.loginBody
import cn.bit101.android.data.repo.base.LoginRefreshResult
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.api.model.common.SchoolLoginResult
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.api.model.http.bit101.PostRegisterDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyInitDataModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.net.CookieManager
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
                loginStatus.status.set(true)
                return@withLock true
            }

            refreshLoginLocked(allowInteractive = true) == LoginRefreshResult.SUCCESS
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

    // 所有登录状态变更均由 loginMutex 保护。
    private suspend fun refreshLoginLocked(allowInteractive: Boolean): LoginRefreshResult {
        val sid = loginStatus.sid.get()
        val password = loginStatus.password.get()
        if (sid.isBlank() || password.isBlank()) {
            loginStatus.status.set(false)
            return LoginRefreshResult.FAILED
        }
        if (!allowInteractive && !loginStatus.status.get()) return LoginRefreshResult.NEEDS_INTERACTIVE

        val result = try {
            authenticate(sid, password, if (allowInteractive) smsCodeRequestHub.createSmsCodeHandler() else null)
            LoginRefreshResult.SUCCESS
        } catch (e: Exception) {
            e.asLoginException("刷新登录").result
        }
        if (result == LoginRefreshResult.FAILED || result == LoginRefreshResult.NEEDS_INTERACTIVE) {
            loginStatus.status.set(false)
        }
        return result
    }

    private suspend fun loginSchool(
        username: String,
        password: String,
        smsCodeHandler: SmsCodeHandler?,
    ): SchoolLoginResult = try {
        api.schoolLogin.login(username, password, smsCodeHandler).also {
            // 提交前校验 Cookie，认证期间不修改当前账号的会话。
            SchoolCookieStore.replace(CookieManager().cookieStore, it.cookies)
        }
    } catch (e: Exception) {
        throw e.asLoginException("学校登录")
    }

    private suspend fun loginBIT101(username: String, password: String): String {
        val initData = api.user.webVpnVerifyInit(
            PostWebvpnVerifyInitDataModel.Body(
                sid = username,
            )
        ).loginBody("初始化社区认证")

        val salt = initData.salt
        val execution = initData.execution
        val cookie = initData.cookie
        if (salt.isBlank() || execution.isBlank() || cookie.isBlank()) throw invalidLoginResponse("初始化社区认证")
        val encryptedPassword = try {
            AESUtils.encryptPassword(password, salt)
        } catch (e: Exception) {
            throw invalidLoginResponse("初始化社区认证", e)
        }

        val verifyData = api.user.webVpnVerify(
            PostWebvpnVerifyDataModel.Body(
                sid = username,
                password = encryptedPassword,
                execution = execution,
                cookie = cookie,
                salt = salt,
                captcha = ""
            )
        ).loginBody("验证学校身份")
        val md5Password = HashUtils.md5(password)
        val token = verifyData.token
        val code = verifyData.code
        if (token.isBlank() || code.isBlank()) throw invalidLoginResponse("验证学校身份")

        val fakeCookie = api.user.register(
            PostRegisterDataModel.Body(
                password = md5Password,
                token = token,
                code = code,
                loginMode = true
            )
        ).loginBody("登录社区").fakeCookie

        if (fakeCookie.isBlank()) throw invalidLoginResponse("登录社区")
        return fakeCookie
    }

    private suspend fun authenticate(username: String, password: String, smsCodeHandler: SmsCodeHandler?) {
        val school = loginSchool(username, password, smsCodeHandler)
        val fakeCookie = loginBIT101(username, password)
        currentCoroutineContext().ensureActive()
        // 两边认证成功才提交；提交期间不因协程取消留下半份登录数据。
        withContext(NonCancellable) {
            if (loginStatus.sid.get() != username) loginStatus.cookieManager.cookieStore.removeAll()
            SchoolCookieStore.replace(loginStatus.cookieManager.cookieStore, school.cookies)
            apiManager.switch(school.webVpn)
            loginStatus.sid.set(username)
            loginStatus.password.set(password)
            loginStatus.fakeCookie.set(fakeCookie)
            loginStatus.status.set(true)
        }
    }

    override suspend fun login(
        username: String,
        password: String,
        smsCodeHandler: SmsCodeHandler?,
    ) = withContext(Dispatchers.IO) {
        loginMutex.withLock {
            try {
                authenticate(username, password, smsCodeHandler)
                true
            } catch (e: Exception) {
                throw e.asLoginException("登录")
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
