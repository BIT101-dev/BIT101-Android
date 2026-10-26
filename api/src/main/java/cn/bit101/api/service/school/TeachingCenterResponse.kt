package cn.bit101.api.service.school

import cn.bit101.bitlogin.http.HttpResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException

class SchoolSessionExpired : IOException("教学中心会话已失效，请重新连接校园账号")
class SchoolAccessDenied : IOException("学校教务服务返回无权访问，请通过学校官方课表入口确认权限")
class SchoolRouteUnavailable(val webVpn: Boolean, cause: Throwable) : IOException("学校服务连接失败", cause)
class SchoolResponseError(message: String) : IOException(message)

internal fun Throwable.isSchoolConnectionFailure(): Boolean = generateSequence(this) { it.cause }
    .take(16).toList().let { causes ->
        causes.none { it is CancellationException } && causes.any {
            it is UnknownHostException || it is ConnectException || it is SocketTimeoutException ||
                it.javaClass.simpleName in setOf("ConnectTimeoutException", "SocketTimeoutException", "HttpRequestTimeoutException")
        }
    }

/** 检查实际响应，不把普通网关错误或 JSON 解析失败误判为登录过期。 */
internal fun HttpResponse.validateTeachingCenter() {
    val html = bodyText.take(16_384).lowercase()
    val isHtml = html.contains("<html") || html.contains("<!doctype html")
    if (isTeachingCenterRefererError()) throw SchoolResponseError("教学中心页面来源校验失败，请重试")
    if (isHtml && (html.contains("无权访问") || html.contains("没有权限"))) throw SchoolAccessDenied()
    if (status == 401 || status == 403) throw SchoolSessionExpired()
    if (isSchoolLoginUrl(url) || location()?.let { isSchoolLoginUrl(URI(url).resolve(it).toString()) } == true ||
        isHtml && listOf("统一身份认证", "用户名密码", "cas/login", "login-page-flowkey").any(html::contains)) {
        throw SchoolSessionExpired()
    }
    if (status !in 200..299) throw SchoolResponseError("教学中心接口返回 HTTP $status")
}

internal fun HttpResponse.isTeachingCenterRefererError(): Boolean = bodyText.take(16_384).let {
    it.contains("Illegal refferer", ignoreCase = true) || it.contains("Illegal referer", ignoreCase = true) ||
        it.contains("此页面不能跨域访问")
}

internal fun isSchoolLoginUrl(value: String): Boolean {
    val uri = runCatching { URI(value) }.getOrNull() ?: return false
    val host = uri.host.orEmpty().lowercase()
    val path = uri.path.orEmpty().lowercase()
    return host == "sso.bit.edu.cn" || path.endsWith("/cas/login") ||
        path.endsWith("/auth-protocol-core/login") || host == "webvpn.bit.edu.cn" && path == "/login"
}

internal fun HttpResponse.teachingCenterJson(): JsonObject {
    validateTeachingCenter()
    val json = try { JsonParser().parse(bodyText).asJsonObject } catch (e: RuntimeException) {
        throw SchoolResponseError("教学中心返回了无法识别的数据，请稍后重试")
    }
    checkBusinessResponse(json)
    return json
}

private fun checkBusinessResponse(value: JsonElement) {
    if (!value.isJsonObject) return
    val obj = value.asJsonObject
    fun field(name: String) = obj.entrySet().firstOrNull { it.key.equals(name, ignoreCase = true) }
        ?.value?.takeIf { it.isJsonPrimitive }?.asString?.trim()
    val explicitError = field("error")?.takeIf(String::isNotBlank)
    val message = explicitError ?: field("msg") ?: field("message")
    val normalized = message.orEmpty().lowercase()
    if (normalized.contains("无权访问") || normalized.contains("没有权限")) throw SchoolAccessDenied()
    if (normalized.contains("会话") && (normalized.contains("过期") || normalized.contains("失效")) ||
        normalized.contains("session expired") || normalized.contains("invalid session") ||
        normalized in setOf("未登录", "请先登录", "用户未登录")) throw SchoolSessionExpired()
    val failureMessage = listOf("失败", "错误", "异常", "未发布", "无效", "不可用", "failed", "failure", "error", "invalid", "unavailable")
        .any(normalized::contains)
    val failed = explicitError != null || field("success") == "false" || field("success") != "true" &&
        (failureMessage || field("code")?.toIntOrNull()?.let { it !in setOf(0, 1, 200) } == true)
    if (failed) throw SchoolResponseError(message?.takeIf { it.isNotBlank() } ?: "教学中心接口返回失败状态")
    // 仅检查响应容器，课程记录中的 code/message 等字段不参与错误判断。
    for ((key, child) in obj.entrySet()) {
        if (key.lowercase() !in setOf("rows", "items", "records", "courses", "list") && child.isJsonObject) checkBusinessResponse(child)
    }
}

internal fun JsonObject.text(name: String): String? = get(name)?.takeIf { it.isJsonPrimitive }?.asString
