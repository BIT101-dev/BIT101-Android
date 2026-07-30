package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.api.model.common.SchoolLoginResult
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.api.model.common.SmsCodeRequest
import cn.bit101.bitlogin.NetworkEnv
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.login.SsoLogin
import cn.bit101.bitlogin.service.JxzxehallLogin

class SchoolLoginService(
    private val logger: Logger = emptyLogger,
) {
    suspend fun login(
        username: String,
        password: String,
        smsCodeHandler: SmsCodeHandler? = null,
    ): SchoolLoginResult {
        logger.info("SchoolLoginService", "开始登录 username=$username webvpnMode=${NetworkEnv.webvpnMode}")
        val initialSession = HttpClient()
        val sso = SsoLogin(
            session = initialSession,
            smsCodeCallback = smsCodeHandler?.let { handler ->
                { context ->
                    logger.info("SchoolLoginService", "请求短信验证码 maskedPhone=${context.maskedPhone} purpose=${context.purpose}")
                    handler.onSmsCode(
                        SmsCodeRequest(
                            phone = context.phone,
                            maskedPhone = context.maskedPhone,
                            purpose = context.purpose,
                        )
                    )
                }
            },
        )
        val login = JxzxehallLogin(sso)

        try {
            login.login(username, password)
            val cookies = login.getSession().cookieDetails().map {
                SchoolCookie(
                    name = it.name,
                    value = it.value,
                    domain = it.domain,
                    path = it.path,
                    secure = it.secure,
                    expiresEpochSeconds = it.expires,
                )
            }
            check(cookies.isNotEmpty()) { "学校登录成功但未返回 Cookie" }
            logger.info("SchoolLoginService", "登录成功 获取到 ${cookies.size} 个 Cookie")
            return SchoolLoginResult(cookies, NetworkEnv.webvpnMode)
        } catch (e: Throwable) {
            logger.err("SchoolLoginService", "登录失败: ${e.message}")
            throw e
        } finally {
            sso.session.close()
            if (sso.session !== initialSession) initialSession.close()
        }
    }
}
