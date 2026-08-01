package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.api.model.common.SmsCodeRequest
import cn.bit101.api.model.http.school.GetCalendarDataModel
import cn.bit101.bitlogin.Config
import cn.bit101.bitlogin.NetworkEnv
import cn.bit101.bitlogin.api.lexue.LexueCalendar
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.login.SsoLogin
import cn.bit101.bitlogin.sso.BitSsoClient

/**
 * 基于 BIT-Login SDK 的乐学 (Moodle) 服务
 */
class SchoolLexueService(
    private val logger: Logger = emptyLogger,
) {
    /**
     * 获取乐学日历订阅链接。
     *
     * 乐学的 Moodle 会话无法靠已保存的 jxzxehall/webvpn Cookie 通过 gateway 静默重登建立
     * (LexueCalendar.establishMoodleSession 依赖统一身份认证会话), 与 SDK LexueCalendarManualTest
     * 保持一致: 以乐学真实回调 {lexue}/login/index.php 作为 CAS service 执行全新 SSO 登录,
     * 再以该会话调用 LexueCalendar。
     */
    suspend fun getCalendarUrl(
        username: String,
        password: String,
        webVpn: Boolean,
        smsCodeHandler: SmsCodeHandler? = null,
    ): String {
        NetworkEnv.ensureInitialized()
        val urls = if (webVpn) Config.Urls.webvpn else Config.Urls.campus
        urls["lexue"]?.let { Config.Urls.active["lexue"] = it }
        val lexue = Config.Urls.active["lexue"] ?: throw RuntimeException("lexue 地址未配置")

        val session = HttpClient().apply {
            BitSsoClient.BROWSER_DEFAULT_HEADERS.forEach { (k, v) -> headers[k] = v }
        }
        val sso = SsoLogin(
            session = session,
            smsCodeCallback = smsCodeHandler?.let { handler ->
                { context ->
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
        try {
            sso.login(username, password, callbackUrl = "$lexue/login/index.php")
            return LexueCalendar(session).getCalendarUrl()
        } catch (e: Throwable) {
            logger.err("SchoolLexueService", "获取日历订阅链接 失败: ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }

    suspend fun getCalendar(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        url: String,
    ): List<GetCalendarDataModel.CalendarEvent> = withSession(cookies, webVpn, "获取日历") { session ->
        LexueCalendar(session).getCalendar(url).map {
            GetCalendarDataModel.CalendarEvent(
                uid = it.uid,
                event = it.event,
                description = it.description,
                course = it.course,
                time = it.time,
            )
        }
    }

    private suspend fun <T> withSession(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        action: String,
        block: suspend (HttpClient) -> T,
    ): T {
        val session = openSchoolSession(cookies, webVpn)
        try {
            return block(session)
        } catch (e: Throwable) {
            logger.err("SchoolLexueService", "$action 失败: ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }
}
