package cn.bit101.android.data.repo

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.AESUtils
import cn.bit101.android.data.common.HashUtils
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.api.model.http.bit101.PostRegisterDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyInitDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class DefaultLoginRepo @Inject constructor(
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus
) : LoginRepo {

    private val api = apiManager.api

    private suspend fun checkSchoolLogin() = withContext(Dispatchers.IO) {
        val initResponse = api.schoolUser.initLogin().body() ?: throw Exception("check login response error")
        val firstLogin = initResponse.ifLogin

        if(firstLogin) true
        else {
            // 可能是Cookie过期
            // 判断是否存有账号和密码
            val sid = loginStatus.sid.get()
            val password = loginStatus.password.get()

            if(sid.isNotEmpty() && password.isNotEmpty()) {
                // 重新登录一次
                loginSchool(
                    username = sid,
                    password = password,
                    salt = initResponse.salt ?: "",
                    execution = initResponse.execution ?: ""
                )
            } else throw Exception("check login failed")
        }
    }

    private suspend fun checkBit101Login() = withContext(Dispatchers.IO) {
        api.user.check().isSuccessful
    }

    override suspend fun checkLogin() = withContext(Dispatchers.IO) {
        val res = checkBit101Login() && checkSchoolLogin()
        if(!res) {
            loginStatus.clear()
        }
        res
    }

    private suspend fun loginSchool(
        username: String,
        password: String,
        salt: String? = null,
        execution: String? = null,
    ) = withContext(Dispatchers.IO) {
        var finalSalt = salt
        var finalExecution = execution

        if(finalSalt == null || finalExecution == null) {
            val initResponse = api.schoolUser.initLogin().body() ?: throw Exception("init login response error")
            if(initResponse.ifLogin) true

            finalSalt = initResponse.salt
            finalExecution = initResponse.execution
        }

        val cryptPassword = AESUtils.encryptPassword(password, finalSalt ?: "")

        val res = api.schoolUser.login(
            username = username,
            password = cryptPassword,
            execution = finalExecution ?: "",
        ).body() ?: throw Exception("login response error")
        res.success
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
        loginStatus.clear()
        val res = loginSchool(username, password) && loginBIT101(username, password)
        if(res) {
            loginStatus.sid.set(username)
            loginStatus.password.set(password)
            loginStatus.status.set(true)
        }
        res
    }

    override suspend fun logout() {
        loginStatus.clear()
    }
}