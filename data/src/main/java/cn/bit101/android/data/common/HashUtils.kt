package cn.bit101.android.data.common

import java.math.BigInteger
import java.security.MessageDigest

/**
 * 关于哈希的工具类，与登录相关
 */
internal object HashUtils {
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(input.toByteArray())
        val no = BigInteger(1, messageDigest)
        var hashtext = no.toString(16)
        while (hashtext.length < 32) {
            hashtext = "0$hashtext"
        }
        return hashtext
    }
}