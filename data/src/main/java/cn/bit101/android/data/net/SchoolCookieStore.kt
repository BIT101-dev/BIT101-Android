package cn.bit101.android.data.net

import cn.bit101.api.model.common.SchoolCookie
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.time.Instant

internal object SchoolCookieStore {
    fun replace(
        cookieStore: CookieStore,
        cookies: List<SchoolCookie>,
        nowEpochSeconds: Long = Instant.now().epochSecond,
    ) {
        val converted = cookies.mapNotNull { it.toHttpCookie(nowEpochSeconds) }
        require(converted.isNotEmpty()) { "学校登录未返回可用 Cookie" }

        cookieStore.cookies
            .filter { it.domain.isSchoolDomain() }
            .forEach { cookieStore.remove(it.uri(), it) }

        converted.forEach { cookieStore.add(it.uri(), it) }
    }

    fun snapshot(
        cookieStore: CookieStore,
        nowEpochSeconds: Long = Instant.now().epochSecond,
    ): List<SchoolCookie> =
        cookieStore.cookies
            .filter { it.domain.isSchoolDomain() && !it.hasExpired() }
            .map { it.toSchoolCookie(nowEpochSeconds) }

    internal fun HttpCookie.toSchoolCookie(nowEpochSeconds: Long): SchoolCookie {
        return SchoolCookie(
            name = name,
            value = value,
            domain = requireNotNull(domain) { "Cookie domain 不能为空" },
            path = path ?: "/",
            secure = secure,
            expiresEpochSeconds = if (maxAge >= 0) nowEpochSeconds + maxAge else null,
        )
    }

    internal fun SchoolCookie.toHttpCookie(nowEpochSeconds: Long): HttpCookie? {
        require(name.isNotBlank()) { "Cookie name 不能为空" }
        require(domain.isNotBlank()) { "Cookie domain 不能为空" }

        val remainingSeconds = expiresEpochSeconds?.minus(nowEpochSeconds)
        if (remainingSeconds != null && remainingSeconds <= 0) return null

        return HttpCookie(name, value).apply {
            domain = this@toHttpCookie.domain
            path = this@toHttpCookie.path.ifBlank { "/" }
            secure = this@toHttpCookie.secure
            maxAge = remainingSeconds ?: -1
        }
    }

    private fun String?.isSchoolDomain(): Boolean {
        val normalized = this?.trimStart('.')?.lowercase() ?: return false
        return normalized == "bit.edu.cn" || normalized.endsWith(".bit.edu.cn")
    }

    private fun HttpCookie.uri(): URI {
        val host = requireNotNull(domain).trimStart('.')
        return URI("${if (secure) "https" else "http"}://$host${path ?: "/"}")
    }
}
