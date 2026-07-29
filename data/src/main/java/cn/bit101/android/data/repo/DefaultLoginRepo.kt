package cn.bit101.android.data.repo

import android.database.sqlite.SQLiteException
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.AESUtils
import cn.bit101.android.data.common.HashUtils
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.api.model.http.bit101.PostRegisterDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyInitDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject


internal class DefaultLoginRepo @Inject constructor(
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus
) : LoginRepo {

    private val api
        get() = apiManager.api

    private val loginMutex = Mutex()

    private suspend fun checkSchoolLogin() = withContext(Dispatchers.IO) {
        val currentApi = api
        val sessionValid = try {
            currentApi.schoolJxzxehallapp.getAppConfig().isSuccessful &&
                currentApi.schoolJxzxehallapp.getCurrentTerm().body()
                    ?.datas?.dqxnxq?.rows?.isNotEmpty() == true
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }

        if (sessionValid) {
            true
        } else {
            val sid = loginStatus.sid.get()
            val password = loginStatus.password.get()

            if (sid.isEmpty() || password.isEmpty()) return@withContext false
            loginSchool(sid, password)
        }
    }

    private suspend fun checkBit101Login() = withContext(Dispatchers.IO) {
        api.user.check().isSuccessful
    }

    override suspend fun checkLogin() = withContext(Dispatchers.IO) {
        loginMutex.withLock {
            val res = try {
                checkBit101Login() && checkSchoolLogin()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            if(!res) {
                withContext(NonCancellable) { loginStatus.clear() }
            }
            res
        }
    }

    private suspend fun loginSchool(
        username: String,
        password: String,
    ) = withContext(Dispatchers.IO) {
        val result = api.schoolLogin.login(username, password)
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

    override suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        loginMutex.withLock {
            loginStatus.clear()
            try {
                loginSchool(username, password)
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
}
