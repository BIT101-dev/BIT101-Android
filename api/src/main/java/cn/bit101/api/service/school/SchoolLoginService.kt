package cn.bit101.api.service.school

import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.login.SsoLogin
import cn.bit101.bitlogin.service.JxzxehallLogin

class SchoolLoginService {
    suspend fun login(username: String, password: String): List<SchoolCookie> {
        val initialSession = HttpClient()
        val sso = SsoLogin(session = initialSession)
        val login = JxzxehallLogin(sso)

        try {
            login.login(username, password)
            return login.getSession().cookieDetails().map {
                SchoolCookie(
                    name = it.name,
                    value = it.value,
                    domain = it.domain,
                    path = it.path,
                    secure = it.secure,
                    expiresEpochSeconds = it.expires,
                )
            }.also { cookies ->
                check(cookies.isNotEmpty()) { "学校登录成功但未返回 Cookie" }
            }
        } finally {
            sso.session.close()
            if (sso.session !== initialSession) initialSession.close()
        }
    }
}
