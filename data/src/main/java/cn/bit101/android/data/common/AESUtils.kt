package cn.bit101.android.data.common

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES 加密工具类，与登录相关
 */
internal object AESUtils {
    fun encryptPassword(password: String, loginCrypto: String): String {
        val passwordBytes = password.toByteArray(Charsets.UTF_8)

        val decodedKey = Base64.getDecoder().decode(loginCrypto)
        val secretKey = SecretKeySpec(decodedKey, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(passwordBytes)
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
}
