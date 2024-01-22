package cn.bit101.android.data.net.basic

data class Cookies(
    val schoolCookie: String,
    val bit101Cookie: String,
) {
    companion object {
        fun empty() = Cookies("", "")
    }
}