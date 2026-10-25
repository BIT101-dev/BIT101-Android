package cn.bit101.android.data.repo

import cn.bit101.android.data.common.HashUtils
import cn.bit101.android.data.repo.base.*
import cn.bit101.api.model.http.bit101.*
import cn.bit101.api.service.bit101.AuthApiService
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/** 主动登录或已知 token 到期时使用；调用方负责合并请求和持久化冷却时间。 */
internal suspend fun AuthApiService.loginWithSchoolIdentity(
    sid: String,
    password: String,
    checkAccount: suspend () -> Unit,
): String {
    checkAccount()
    val init = webVpnVerifyInit(PostWebvpnVerifyInitDataModel.Body(sid)).loginBody("初始化统一认证")
    if (init.salt.isBlank() || init.execution.isBlank() || init.cookie.isBlank()) {
        throw invalidLoginResponse("初始化统一认证")
    }
    if (init.captcha.isNotBlank()) throw LoginException(
        LoginFailure.NEEDS_INTERACTIVE, "统一认证", "学校要求图形验证码，请通过 BIT101 网页完成登录或使用 BIT101 密码登录。",
    )
    val encrypted = try {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(Base64.getDecoder().decode(init.salt), "AES"))
        Base64.getEncoder().encodeToString(cipher.doFinal(password.toByteArray(Charsets.UTF_8)))
    } catch (e: Exception) {
        throw invalidLoginResponse("初始化统一认证", e)
    }
    checkAccount()
    val proof = webVpnVerify(PostWebvpnVerifyDataModel.Body(
        sid = sid, password = encrypted, execution = init.execution,
        cookie = init.cookie, salt = init.salt, captcha = "",
    )).loginBody("验证学校身份")
    if (proof.token.isBlank() || proof.code.isBlank()) throw invalidLoginResponse("验证学校身份")
    checkAccount()
    return register(PostRegisterDataModel.Body(
        password = HashUtils.md5(password), token = proof.token, code = proof.code, loginMode = true,
    )).loginBody("登录 BIT101").fakeCookie
}
