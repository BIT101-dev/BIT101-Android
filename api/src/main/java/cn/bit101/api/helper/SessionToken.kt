package cn.bit101.api.helper

import com.google.gson.JsonParser
import java.util.Base64

/** 只读取到期时间用于安排续期；不承担 token 验签或身份认证。 */
fun tokenExpiresAt(token: String): Long? = runCatching {
    JsonParser().parse(String(Base64.getUrlDecoder().decode(token.split('.')[1])))
        .asJsonObject["exp"].asLong
}.getOrNull()
