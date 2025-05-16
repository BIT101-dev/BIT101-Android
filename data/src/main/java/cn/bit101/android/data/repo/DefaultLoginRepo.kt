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


internal class DefaultLoginRepo @Inject constructor(
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

        val cryptPassword = AESUtils.encryptPasswordNew(password, finalSalt ?: "")

        val res = api.schoolUser.login(
            username = username,
            password = cryptPassword,
            execution = finalExecution ?: "",
            salt = finalSalt ?: "",
            captchaPayload = AESUtils.encryptPasswordNew("{}", finalSalt ?: "") // 去掉似乎也没问题
            // 不需要验证码:
            // 这里模拟的是正式的登录请求 (对应开发者工具的 Network 里的 login)
            // 官方网页端会在发送这个正式登录请求前先发送一个验证码请求, 检查是否需要验证码 (/cas/api/protected/user/findCaptchaCount/{学号}?{一串数字})
            // 发现需要验证码后就会请求验证码图片 (默认是 /cas/api/captcha/generate/DEFAULT)
            // 在请求完这个图片后, 这个登录请求里就必须附上正确(和最后一个请求的验证码图片匹配)的验证码才能成功了
            // 没错, 这意味着只要不请求验证码, 就不需要验证码, 完美的唯心主义登录
            // 可以自己实验一下: 正常在官方网页端输错三次密码, 显示出验证码后, 手动开新标签页访问上面那个验证码图片链接, 然后输入里面的验证码(而不是网页端此时显示的那个), 你会发现登录成功了, 即使你输入的是"错误的"验证码
            // 另一个实验: 先在网页端输错三次密码启用验证码, 再开一个新无痕窗口访问登录页面, 此时页面上不会显示验证码, 但当你点击登录按钮时验证码框就会弹出来
            //           在验证码框弹出来前, 用开发者工具替换掉 main-es2015.d28ec9f63aa61a984122.js, 删去 9021~9033 行(改完保存完记得刷新), 再点击登录, 就直接登录成功了
            //           但如果你在验证码弹出来后再做如上替换, 登录就会失败(查看 POST 的 Response 也会发现确实失败了)
            // 所以理论上应该是完全不需要验证码的
            // 顺便那个 captchaPayload 对应的 Json 一直是空的, 哪怕网页端在有验证码时发的 POST 里面的这个 Json 都是空的, 翻 js 代码发现里面似乎只会存意义不明的错误码
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