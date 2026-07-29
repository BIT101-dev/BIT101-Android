package cn.bit101.api.model.common

data class SchoolCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val expiresEpochSeconds: Long?,
)

data class SchoolLoginResult(
    val cookies: List<SchoolCookie>,
    val webVpn: Boolean,
)
