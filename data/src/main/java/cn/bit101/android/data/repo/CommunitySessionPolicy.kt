package cn.bit101.android.data.repo

import cn.bit101.api.helper.tokenExpiresAt
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal object CommunitySessionPolicy {
    const val IDENTITY_RETRY_INTERVAL = 24 * 60 * 60_000L

    fun expiresSoon(token: String, now: Long): Boolean =
        tokenExpiresAt(token)?.let { it <= now / 1000 + 60 } ?: false

    fun usable(token: String, now: Long): Boolean {
        if (token.isBlank()) return false
        // 只用 exp 安排续期，身份与签名始终由服务端校验；未知格式交给业务响应判断。
        val expiry = tokenExpiresAt(token) ?: return true
        return expiry > now / 1000 + 60
    }

    fun retryAt(now: Long, attempt: Int, retryAfter: String?, identity: Boolean = false): Long {
        val delay = if (identity) IDENTITY_RETRY_INTERVAL * (1L shl (attempt.coerceIn(1, 4) - 1))
            else when (attempt) { 1 -> 30 * 60_000L; 2 -> 2 * 60 * 60_000L; else -> 24 * 60 * 60_000L }
        val requested = retryAfter?.toLongOrNull()?.takeIf { it >= 0 }?.let {
            runCatching { Math.addExact(now, Math.multiplyExact(it, 1000L)) }.getOrNull()
        }
            ?: runCatching { ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }.getOrNull()
        return maxOf(now + delay, requested ?: 0)
    }
}
