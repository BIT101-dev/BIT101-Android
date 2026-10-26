package cn.bit101.api.service.school

import cn.bit101.bitlogin.http.HttpResponse
import org.junit.Assert.*
import org.junit.Test
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException

class TeachingCenterResponseTest {
    private fun response(body: String, status: Int = 200, url: String = "https://jxzxehallapp.bit.edu.cn/api") =
        HttpResponse(status, emptyMap(), body, url)

    @Test fun `200 login HTML and final CAS redirect expire the session`() {
        assertThrows(SchoolSessionExpired::class.java) {
            response("<!doctype html><html>统一身份认证</html>").teachingCenterJson()
        }
        assertThrows(SchoolSessionExpired::class.java) {
            response("{}", url = "https://sso.bit.edu.cn/cas/login").teachingCenterJson()
        }
        assertThrows(SchoolSessionExpired::class.java) {
            HttpResponse(302, mapOf("Location" to "/cas/login"), "", "https://sso.bit.edu.cn/").validateTeachingCenter()
        }
    }

    @Test fun `explicit permission errors take precedence over login markers`() {
        assertThrows(SchoolAccessDenied::class.java) {
            response("<html>统一身份认证：无权访问该资源</html>", 403).teachingCenterJson()
        }
        assertThrows(SchoolSessionExpired::class.java) { response("{}", 403).teachingCenterJson() }
    }

    @Test fun `gateway HTML and malformed JSON do not trigger reauthentication`() {
        for (body in listOf("<html>Bad gateway</html>", "{", "[]")) {
            assertThrows(SchoolResponseError::class.java) { response(body).teachingCenterJson() }
        }
        assertThrows(SchoolResponseError::class.java) { response("unavailable", 503).teachingCenterJson() }
    }

    @Test fun `HTTP 200 referer error page is not a valid teaching session`() {
        val error = assertThrows(SchoolResponseError::class.java) {
            response("<html>#E2140100001 Illegal refferer. 此页面不能跨域访问</html>").validateTeachingCenter()
        }
        assertEquals("教学中心页面来源校验失败，请重试", error.message)
    }

    @Test fun `nested business failures are not mistaken for an empty schedule`() {
        assertThrows(SchoolSessionExpired::class.java) {
            response("""{"datas":{"cxxszhxqkb":{"rows":[],"extParams":{"code":3,"msg":"会话已过期"}}}}""").teachingCenterJson()
        }
        assertThrows(SchoolResponseError::class.java) {
            response("""{"datas":{"cxxszhxqkb":{"rows":[],"extParams":{"code":3,"msg":"课表尚未发布"}}}}""").teachingCenterJson()
        }
        assertThrows(SchoolAccessDenied::class.java) {
            response("""{"success":false,"msg":"无权访问该资源"}""").teachingCenterJson()
        }
    }

    @Test fun `valid empty schedules and course record fields are preserved`() {
        response("""{"datas":{"cxxszhxqkb":{"rows":[],"extParams":{"code":1}}}}""").teachingCenterJson()
        response("""{"datas":{"cxxszhxqkb":{"rows":[{"code":500,"message":"error"}]}}}""").teachingCenterJson()
    }

    @Test fun `business error message inside successful HTTP envelope cannot erase courses`() {
        for (body in listOf(
            """{"datas":{"cxxszhxqkb":{"rows":[],"extParams":{"code":200,"msg":"课表尚未发布"}}}}""",
            """{"datas":{"cxxszhxqkb":{"rows":[],"extParams":{"Message":"查询失败"}}}}""",
            """{"error":"查询被拒绝","data":[]}""",
        )) {
            assertThrows(SchoolResponseError::class.java) { response(body).teachingCenterJson() }
        }
    }

    @Test fun `route recovery recognizes wrapped connection errors but preserves cancellation`() {
        assertTrue(RuntimeException(UnknownHostException()).isSchoolConnectionFailure())
        assertFalse(RuntimeException(CancellationException()).isSchoolConnectionFailure())
        assertFalse(SchoolResponseError("bad response").isSchoolConnectionFailure())
    }
    @Test fun `successful callback is distinct from the login entry`() {
        response("<html>认证成功</html>", url = "https://jxzxehall.bit.edu.cn/auth-protocol-core/loginSuccess").validateTeachingCenter()
    }

}
