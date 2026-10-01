package cn.bit101.api.option

data class ApiUrlOption(
    val bit101Url: String,
    val androidUrl: String,
    val jwmsUrl: String,
    val jwcUrl: String,
    val jxzxehallappUrl: String,
    val lexueUrl: String,
    /** 课程中心 API 地址。 */
    val eclassUrl: String = "https://zy-eclass.bit.edu.cn",
)
