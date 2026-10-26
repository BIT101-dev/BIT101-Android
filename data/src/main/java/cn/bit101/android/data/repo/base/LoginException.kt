package cn.bit101.android.data.repo.base

import cn.bit101.api.service.school.SchoolAccessDenied
import cn.bit101.api.service.school.SchoolResponseError
import cn.bit101.api.service.school.SchoolSessionExpired
import cn.bit101.bitlogin.sso.CaptchaError
import cn.bit101.bitlogin.sso.LoginError
import cn.bit101.bitlogin.sso.SmsVerificationError
import cn.bit101.bitlogin.sso.SsoHttpException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

enum class LoginFailure { NEEDS_INTERACTIVE, TRANSIENT, FAILED, ERROR }

class LoginException internal constructor(
    val result: LoginFailure,
    val step: String,
    message: String,
    val statusCode: Int? = null,
    val retryAfter: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

internal fun <T> Response<T>.loginBody(step: String): T {
    if (!isSuccessful) {
        errorBody()?.close()
        throw httpLoginException(step, code(), headers()["Retry-After"])
    }
    return body() ?: throw invalidLoginResponse(step)
}

internal fun invalidLoginResponse(step: String, cause: Throwable? = null) = LoginException(
    LoginFailure.ERROR, step, "认证服务返回了异常数据，请稍后重试。", cause = cause,
)

private fun httpLoginException(step: String, status: Int, retryAfter: String? = null, cause: Throwable? = null) =
    LoginException(
        result = if (status == 429 || status in 500..599) LoginFailure.TRANSIENT else LoginFailure.ERROR,
        step = step,
        message = when (status) {
            429 -> "认证请求过于频繁，请稍后重试。"
            in 500..599 -> "认证服务暂时不可用，请稍后重试。"
            else -> "认证请求未通过，请稍后重试。"
        },
        statusCode = status,
        retryAfter = retryAfter,
        cause = cause,
    )

internal fun Throwable.asLoginException(step: String): LoginException {
    val causes = mutableListOf<Throwable>()
    var current: Throwable? = this
    while (current != null && causes.none { it === current }) {
        causes += current
        current = current.cause
    }
    causes.filterIsInstance<CancellationException>().firstOrNull()?.let { throw it }
    causes.filterIsInstance<LoginException>().firstOrNull()?.let { return it }
    for (cause in causes) {
        when (cause) {
            is CaptchaError, is SmsVerificationError -> return LoginException(
                LoginFailure.NEEDS_INTERACTIVE, step, "需要完成验证码验证，请重新登录。", cause = this,
            )
            is SsoHttpException -> return httpLoginException(step, cause.statusCode, cause = this)
            is LoginError -> {
                if (cause.statusCode == 429 || cause.statusCode in 500..599) {
                    return httpLoginException(step, cause.statusCode!!, cause = this)
                }
                if (cause.code in setOf("1030027", "1030028", "1030031", "1330001", "1410040", "1410041", "3910001")) {
                    return LoginException(LoginFailure.FAILED, step, "账号或密码未通过验证，请检查后重新登录。", cause = this)
                }
            }
            is SchoolAccessDenied, is SchoolResponseError, is SchoolSessionExpired -> return LoginException(
                LoginFailure.ERROR, step, cause.message.orEmpty(), cause = this,
            )
            is IOException -> return LoginException(
                LoginFailure.TRANSIENT, step, "网络连接失败，请稍后重试。", cause = this,
            )
        }
    }
    return invalidLoginResponse(step, this)
}
