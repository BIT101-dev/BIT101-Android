package cn.bit101.api.service.school

import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.Config
import cn.bit101.bitlogin.NetworkEnv
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.sso.BitSsoClient
import java.time.Instant

private const val WEBVPN_HOST = "webvpn.bit.edu.cn"

/**
 * 基于已登录 Cookie 构建 BIT-Login SDK 会话, 并让 SDK 的服务地址与 App 的 WebVPN 设置保持一致
 */
internal suspend fun openSchoolSession(
    cookies: List<SchoolCookie>,
    webVpn: Boolean,
): HttpClient {
    NetworkEnv.ensureInitialized()

    val urls = if (webVpn) Config.Urls.webvpn else Config.Urls.campus
    urls["jxzxehall_app"]?.let { Config.Urls.active["jxzxehall_app"] = it }
    urls["lexue"]?.let { Config.Urls.active["lexue"] = it }

    // 与 SDK SsoLogin 保持一致: 会话必须携带浏览器默认请求头。
    // 乐学的统一身份认证 gateway 静默重登依赖该 UA 指纹, 否则
    // CAS/authserver 会返回非预期页面, 导致 Moodle 会话建立失败
    val session = HttpClient().apply {
        BitSsoClient.BROWSER_DEFAULT_HEADERS.forEach { (k, v) -> headers[k] = v }
    }
    val nowEpochSeconds = Instant.now().epochSecond
    cookies.forEach { cookie ->
        val expires = cookie.expiresEpochSeconds
        if (expires == null || expires > nowEpochSeconds) {
            session.addCookie(
                name = cookie.name,
                value = cookie.value,
                domain = cookie.domain,
                path = cookie.path.ifBlank { "/" },
                secure = cookie.secure,
                expiresEpochSeconds = expires,
            )
        }
    }

    // WebVPN 模式下, 校内域名的 Cookie (如 _WEU) 不会发往 webvpn 主机,
    // 复制一份到 webvpn 域名下, 让网关转发给后端服务
    if (webVpn) {
        cookies.filter {
            it.domain.trimStart('.').lowercase() != WEBVPN_HOST
                && it.domain.trimStart('.').lowercase().endsWith(".bit.edu.cn")
        }.forEach { cookie ->
            val expires = cookie.expiresEpochSeconds
            if (expires == null || expires > nowEpochSeconds) {
                session.addCookie(
                    name = cookie.name,
                    value = cookie.value,
                    domain = WEBVPN_HOST,
                    path = "/",
                    secure = cookie.secure,
                    expiresEpochSeconds = expires,
                )
            }
        }
    }

    return session
}
