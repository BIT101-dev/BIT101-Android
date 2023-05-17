package cn.bit101.android.net.bit101

/**
 * @author flwfdd
 * @date 2023/5/18 上午1:39
 * @description _(:з」∠)_
 */

// 初始化统一身份认证
data class WebvpnVerifyInitRequest(
    val sid: String,
)

data class WebvpnVerifyInitResponse(
    val captcha: String,
    val cookie: String,
    val execution: String,
    val salt: String
)

// 统一身份认证
data class WebvpnVerifyRequest(
    val sid: String,
    val password: String,
    val execution: String,
    val cookie: String,
    val captcha: String,
)

data class WebvpnVerifyResponse(
    val token: String,
    val code: String
)

// 注册
data class RegisterRequest(
    val password: String,
    val token: String,
    val code: String,
    val login_mode: Boolean
)

data class RegisterResponse(
    val fake_cookie: String
)
