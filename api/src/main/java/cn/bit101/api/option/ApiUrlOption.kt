package cn.bit101.api.option

data class ApiUrlOption(
    val bit101Url: String,
    val androidUrl: String,
    val jwmsUrl: String,
    val jwcUrl: String,
    val jxzxehallappUrl: String,
    val lexueUrl: String,
    /**
     * 课程中心（eclass）。学校 2026 年起用它替代乐学下发作业与学习资料。
     *
     * 校外访问走统一身份认证（Keycloak）而非 WebVPN 隧道，故三个环境同址。
     * 默认值让既有构造点无需改动。
     */
    val eclassUrl: String = "https://zy-eclass.bit.edu.cn",
)
