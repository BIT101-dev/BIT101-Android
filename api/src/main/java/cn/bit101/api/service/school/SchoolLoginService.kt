package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.SchoolLoginResult
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.api.model.common.SmsCodeRequest
import cn.bit101.bitlogin.Config
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.http.HttpResponse
import cn.bit101.bitlogin.login.LoginResult
import cn.bit101.bitlogin.login.SsoLogin
import cn.bit101.bitlogin.util.NetEnv
import cn.bit101.bitlogin.util.WebVpnUrl
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SchoolLoginService internal constructor(
    private val logger: Logger,
    private val createClient: () -> HttpClient,
    private val createSso: (HttpClient, SmsCodeHandler?) -> SsoLogin,
) {
    constructor(logger: Logger = emptyLogger) : this(logger, { HttpClient() }, { session, handler ->
        SsoLogin(session = session, smsCodeCallback = handler?.let {
            { context -> it.onSmsCode(SmsCodeRequest(context.phone, context.maskedPhone, context.purpose)) }
        })
    })
    suspend fun login(
        username: String,
        password: String,
        smsCodeHandler: SmsCodeHandler? = null,
        webVpn: Boolean? = null,
    ): SchoolLoginResult {
        val preferred = webVpn ?: withContext(Dispatchers.IO) { !NetEnv.checkNetworkEnv() }
        try {
            return authenticate(username, password, smsCodeHandler, preferred)
        } catch (e: Exception) {
            // 自动选路仅在连接失败时改走另一条路线；明确指定路线时由调用方控制重试次数。
            if (webVpn != null || e !is SchoolRouteUnavailable) throw e
            return authenticate(username, password, smsCodeHandler, !preferred)
        }
    }

    private suspend fun authenticate(username: String, password: String, handler: SmsCodeHandler?, webVpn: Boolean): SchoolLoginResult {
        val session = createClient()
        val sso = createSso(session, handler)
        try {
            val login = TeachingCenterLogin(session, sso, webVpn)
            val warning = try {
                login.authenticate(username, password)
                null
            } catch (e: Exception) {
                // SDK 可能包装协程取消，任何阶段取消都不得提交登录结果。
                generateSequence<Throwable>(e) { it.cause }.take(16)
                    .filterIsInstance<CancellationException>().firstOrNull()?.let { throw it }
                if (!login.identityVerified) {
                    // 教学中心预检可能先于 SSO 失败；只通过已知的学校服务核验一次身份。
                    if (login.identityAttempted) throw e
                    login.verifyIdentity(username, password)
                }
                when (e) {
                    is SchoolAccessDenied -> "学校教务服务未授予访问权限"
                    is SchoolSessionExpired -> "教学中心会话未建立"
                    is SchoolRouteUnavailable -> "无法连接学校教务服务"
                    is SchoolResponseError -> e.message.orEmpty()
                    else -> "学校教务服务暂时不可用"
                }
            }
            currentCoroutineContext().ensureActive()
            val cookies = session.schoolCookies().filter {
                it.expiresEpochSeconds == null || it.expiresEpochSeconds > Instant.now().epochSecond
            }
            val notice = warning ?: if (cookies.isEmpty()) "学校会话尚未建立" else null
            if (notice != null) logger.warn("SchoolLoginService", "校园身份认证成功；$notice")
            logger.info("SchoolLoginService", "校园身份认证成功 webVpn=$webVpn")
            return SchoolLoginResult(cookies, webVpn, notice)
        } finally {
            session.close()
        }
    }
}

/** 保留 SDK 的密码、验证码协议，业务回调明确绑定课表应用。 */
internal class TeachingCenterLogin(private val session: HttpClient, private val sso: SsoLogin, private val webVpn: Boolean) {
    var identityVerified = false
        private set

    var identityAttempted = false
        private set

    suspend fun verifyIdentity(username: String, password: String) {
        loginIdentity(username, password, Config.Urls.webvpn.getValue("webvpn_cb"), webvpnMode = true)
    }

    private suspend fun loginIdentity(username: String, password: String, callbackUrl: String, webvpnMode: Boolean = false): LoginResult {
        identityAttempted = true
        val result = sso.login(username, password, callbackUrl = callbackUrl, webvpnMode = webvpnMode)
        identityVerified = true
        return result
    }

    suspend fun authenticate(username: String, password: String) {
        if (webVpn) {
            val fingerprint = UUID.randomUUID().toString().replace("-", "")
            val fingerprintResponse = request("https://webvpn.bit.edu.cn/set-fingerprint?fingerprint=$fingerprint")
            // 未登录时这里正常返回 302 /login；此时尚未开始 SSO，不是业务会话失效。
            if (!fingerprintResponse.isRedirect()) fingerprintResponse.validateTeachingCenter()
            val result = loginIdentity(username, password, Config.Urls.webvpn.getValue("webvpn_cb"), webvpnMode = true)
            follow(result.callback).validateTeachingCenter()
            val now = Instant.now().epochSecond
            if (session.cookieDetails().none {
                it.domain.trimStart('.') == "webvpn.bit.edu.cn" &&
                    it.name in setOf("wengine_vpn_ticket", "wengine_vpn_ticketwebvpn_bit_edu_cn") &&
                    it.value.isNotBlank() && (it.expires == null || it.expires!! > now)
            }) throw SchoolResponseError("学校 WebVPN 未建立有效会话")
        }
        val landing = "https://jxzxehallapp.bit.edu.cn/jwapp/sys/wdkbby/*default/index.do"
        val entry = "https://jxzxehall.bit.edu.cn/auth-protocol-core/login?service=" + URLEncoder.encode(landing, "UTF-8")
        val response = request(entry)
        if (!response.isRedirect()) {
            response.validateTeachingCenter()
            throw SchoolResponseError("教学中心未返回认证跳转地址")
        }
        val location = response.location()?.let { URI(entry).resolve(it) }
            ?: throw SchoolResponseError("教学中心未返回认证跳转地址")
        val service = location.rawQuery?.split('&')?.firstOrNull { it.substringBefore('=') == "service" }
            ?.substringAfter('=')?.let { URLDecoder.decode(it, "UTF-8") }
            ?: throw SchoolResponseError("教学中心认证跳转缺少 service 参数")
        requireSchoolUrl(service)
        val result = loginIdentity(username, password, service)
        val callbackPage = follow(result.callback)
        // SSO 跳转可能触发学校的来源校验；下方独立打开首页，相当于浏览器刷新一次。
        if (!callbackPage.isTeachingCenterRefererError()) callbackPage.validateTeachingCenter()
        // 验证课表落地页，避免仅凭任意 SSO Cookie 宣告业务会话可用。
        val page = request(landing, followRedirects = true)
        page.validateTeachingCenter()
        val expected = URI(route(landing))
        val actual = URI(page.url)
        if (actual.host != expected.host || !actual.path.startsWith(expected.path.substringBefore("/*default"))) {
            throw SchoolResponseError("教学中心未进入课表应用")
        }
    }

    private suspend fun follow(callback: String): HttpResponse {
        var next = URI("https://webvpn.bit.edu.cn").resolve(callback).toString()
        repeat(12) {
            requireSchoolUrl(next)
            val response = request(next)
            if (!response.isRedirect()) return response
            next = URI(response.url).resolve(response.location()
                ?: throw SchoolResponseError("学校认证跳转缺少地址")).toString()
        }
        throw SchoolResponseError("学校认证跳转次数过多")
    }

    private fun route(url: String): String = if (webVpn && URI(url).host in setOf("jxzxehall.bit.edu.cn", "jxzxehallapp.bit.edu.cn"))
        WebVpnUrl.convertToWebvpnUrl(url) else url

    private suspend fun request(url: String, followRedirects: Boolean = false): HttpResponse = try {
        session.get(route(url), allowRedirects = followRedirects)
    } catch (e: Exception) {
        if (e.isSchoolConnectionFailure()) throw SchoolRouteUnavailable(webVpn, e)
        throw e
    }

    private fun requireSchoolUrl(url: String) {
        val uri = URI(url)
        if (uri.scheme != "https" || uri.userInfo != null || uri.port !in setOf(-1, 443) ||
            uri.host?.endsWith(".bit.edu.cn") != true) throw SchoolResponseError("学校认证返回了非预期的跳转地址")
    }
}
