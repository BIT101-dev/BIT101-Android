package cn.bit101.android.status

import android.util.Log
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.datastore.UserDataStore
import cn.bit101.android.net.APIManager
import cn.bit101.android.net.BIT101API
import cn.bit101.android.status.base.LoginStatusManager
import cn.bit101.android.utils.AESUtils
import cn.bit101.android.utils.HashUtils
import cn.bit101.api.model.http.bit101.PostRegisterDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyInitDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultLoginStatusManager @Inject constructor(

) : LoginStatusManager {
    private suspend fun checkSchoolLogin() = withContext(Dispatchers.IO) {
        val response = BIT101API.schoolUser.initLogin().body()
        val firstLogin = response?.ifLogin ?: throw Exception("check login response error")

        if(firstLogin) true
        else {
            // 可能是Cookie过期
            // 判断是否存有账号和密码
            val sid = UserDataStore.loginSid.get()
            val password = UserDataStore.loginPassword.get()

            if(sid.isNotEmpty() && password.isNotEmpty()) {
                // 重新登录一次
                loginSchool(sid, password, response.salt ?: "", response.execution ?: "")

                // 没有报错就说明登录成功
                true
            } else false
        }
    }

    private suspend fun checkBit101Login() = withContext(Dispatchers.IO) {
        BIT101API.user.check().isSuccessful
    }

    override suspend fun checkLogin() = withContext(Dispatchers.IO) {
        checkSchoolLogin() && checkBit101Login()
    }

    private suspend fun loginSchool(
        username: String,
        password: String,
        _salt: String? = null,
        _execution: String? = null,
    ) = withContext(Dispatchers.IO) {
        var salt = _salt
        var execution = _execution

        if(salt == null || execution == null) {
            val initData = BIT101API.schoolUser.initLogin().body() ?: throw Exception("init school login error")
            if(initData.ifLogin) return@withContext

            salt = initData.salt
            execution = initData.execution
        }
        // 查看是否需要验证码
//        val needCaptcha = BIT101API.schoolUser.checkNeedCaptcha(
//            username = username,
//        ).body()?.isNeed ?: throw Exception("check need captcha response error")
//
//        if(needCaptcha) {
//            val captcha = BIT101API.schoolUser.getCaptcha().body() ?: throw Exception("get captcha response error")
//            Log.i("loginSchool", "loginSchool: captcha: $captcha")
//        }

        val cryptPassword = AESUtils.encryptPassword(password, salt ?: "")

        Log.i("loginSchool", "loginSchool: salt: $salt, execution: $execution, encryptedPassword: $cryptPassword")


        val loginResponse = BIT101API.schoolUser.login(
            username = username,
            password = cryptPassword,
            execution = execution ?: "",
        )

        Log.i("loginSchool", "loginSchool: ${loginResponse.headers().toMultimap()}")
        Log.i("loginSchool", "loginSchool: ${loginResponse.code()}")

        val success = loginResponse.body()?.success ?: throw Exception("get login response error")
        if(!success) throw Exception("login error")

        Log.i("loginSchool", "loginSchool: ${loginResponse.headers().toMultimap()}")
    }

    private suspend fun loginBIT101(sid: String, password: String) = withContext(Dispatchers.IO) {
        val initData = BIT101API.user.webVpnVerifyInit(
            PostWebvpnVerifyInitDataModel.Body(
                sid = sid,
            )
        ).body() ?: throw Exception("init bit101 login error")

        val salt = initData.salt
        val execution = initData.execution
        val cookie = initData.cookie
        val encryptedPassword = AESUtils.encryptPassword(password, salt)

        val verifyData = BIT101API.user.webVpnVerify(
            PostWebvpnVerifyDataModel.Body(
                sid = sid,
                password = encryptedPassword,
                execution = execution,
                cookie = cookie,
                captcha = ""
            )
        ).body() ?: throw Exception("get webVpnVerify response error")
        val md5Password = HashUtils.md5(password)
        val token = verifyData.token
        val code = verifyData.code

        BIT101API.user.register(
            PostRegisterDataModel.Body(
                password = md5Password,
                token = token,
                code = code,
                loginMode = true
            )
        ).body()?.fakeCookie ?: throw Exception("get register response error")
    }

    private suspend fun forget() = withContext(Dispatchers.IO) {
        UserDataStore.clear()
        APIManager.clearCookie()
    }

    override suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        try {
            loginSchool(username, password)
            val fakeCookie = loginBIT101(username, password)

            UserDataStore.loginStatus.set(true)

            UserDataStore.loginSid.set(username)
            UserDataStore.loginPassword.set(password)
            UserDataStore.fakeCookie.set(fakeCookie)

            true
        } catch (e: Exception) {
            forget()
            false
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        forget()
    }
}