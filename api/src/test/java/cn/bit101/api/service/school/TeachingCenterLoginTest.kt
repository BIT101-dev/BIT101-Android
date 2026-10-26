package cn.bit101.api.service.school

import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.http.HttpResponse
import cn.bit101.bitlogin.http.CookieDetail
import cn.bit101.bitlogin.login.LoginResult
import cn.bit101.bitlogin.login.SsoLogin
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import cn.bit101.api.helper.emptyLogger
import java.net.ConnectException
import org.junit.Assert.*
import org.junit.Test
import java.net.URLEncoder

class TeachingCenterLoginTest {
    private val landing = "https://jxzxehallapp.bit.edu.cn/jwapp/sys/wdkbby/*default/index.do"
    private val callback = "https://jxzxehall.bit.edu.cn/auth-protocol-core/callback?ticket=fixture"
    private val session = mockk<HttpClient>()
    private val sso = mockk<SsoLogin>()

    private fun arrange() {
        every { session.cookieDetails() } returns listOf(CookieDetail("wengine_vpn_ticket", "fixture", "webvpn.bit.edu.cn", "/", true, null))
        coEvery { session.get(any(), any(), any()) } coAnswers {
            val url = firstArg<String>()
            when {
                url.contains("/auth-protocol-core/login") -> {
                    assertTrue(url.contains("wdkbby"))
                    assertFalse(url.contains("xsfacx"))
                    HttpResponse(302, mapOf("Location" to "https://sso.bit.edu.cn/cas/login?service=" + URLEncoder.encode(callback.substringBefore('?'), "UTF-8")), "", url)
                }
                url.contains("/auth-protocol-core/callback") -> HttpResponse(302, mapOf("Location" to landing), "", url)
                else -> HttpResponse(200, emptyMap(), "<html>课表</html>", url)
            }
        }
        coEvery { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
            val service = arg<String>(2)
            LoginResult(emptyMap(), if (service.contains("webvpn.bit.edu.cn")) "https://webvpn.bit.edu.cn/" else callback)
        }
    }

    @Test fun `direct login binds CAS service to timetable and follows callback`() = runBlocking<Unit> {
        arrange()
        TeachingCenterLogin(session, sso, false).authenticate("student", "password")
        coVerify(exactly = 1) { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify { session.get(landing, any(), any()) }
    }

    @Test fun `VPN login rewrites each teaching center redirect without global mode`() = runBlocking<Unit> {
        arrange()
        TeachingCenterLogin(session, sso, true).authenticate("student", "password")
        coVerify(exactly = 2) { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { session.get(match { it.startsWith("https://jxzxehall") }, any(), any()) }
        coVerify { session.get(match { it.startsWith("https://webvpn.bit.edu.cn/") && it.contains("/wdkbby/") }, any(), any()) }
    }

    @Test fun `VPN fingerprint login redirect proceeds to SSO`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(match { it.contains("/set-fingerprint?") }, any(), any()) } coAnswers {
            HttpResponse(302, mapOf("Location" to "/login"), "", firstArg())
        }
        TeachingCenterLogin(session, sso, true).authenticate("student", "password")
        coVerify(exactly = 2) { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test fun `VPN fingerprint server error stops before SSO`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(match { it.contains("/set-fingerprint?") }, any(), any()) } coAnswers {
            HttpResponse(503, emptyMap(), "unavailable", firstArg())
        }
        assertThrows(SchoolResponseError::class.java) { runBlocking {
            TeachingCenterLogin(session, sso, true).authenticate("student", "password")
        } }
        coVerify(exactly = 0) { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test fun `SSO referer error is recovered by opening landing once without another login`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(landing, any(), any()) } returnsMany listOf(
            HttpResponse(200, emptyMap(), "<html>Illegal refferer. 此页面不能跨域访问</html>", landing),
            HttpResponse(200, emptyMap(), "<html>课表</html>", landing),
        )
        TeachingCenterLogin(session, sso, false).authenticate("student", "password")
        coVerify(exactly = 2) { session.get(landing, any(), any()) }
        coVerify(exactly = 1) { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test fun `persistent referer rejection is reported without repeated refresh`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(landing, any(), any()) } returns
            HttpResponse(200, emptyMap(), "<html>Illegal refferer.</html>", landing)
        assertEquals("教学中心页面来源校验失败，请重试", service().login("student", "password", webVpn = false).teachingCenterWarning)
        coVerify(exactly = 2) { session.get(landing, any(), any()) }
    }

    @Test fun `CAS cookies cannot hide a denied timetable landing`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(landing, any(), any()) } returns HttpResponse(200, emptyMap(), "<html>无权访问该资源</html>", landing)
        val login = TeachingCenterLogin(session, sso, false)
        assertThrows(SchoolAccessDenied::class.java) { runBlocking { login.authenticate("student", "password") } }
        assertTrue(login.identityVerified)
    }

    @Test fun `unexpected service URL is rejected before password submission`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(match { it.contains("/auth-protocol-core/login") }, any(), any()) } returns
            HttpResponse(302, mapOf("Location" to "https://sso.bit.edu.cn/cas/login?service=https%3A%2F%2Fexample.com%2F"), "", "https://jxzxehall.bit.edu.cn/")
        assertThrows(SchoolResponseError::class.java) { runBlocking { TeachingCenterLogin(session, sso, false).authenticate("student", "password") } }
        coVerify(exactly = 0) { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) }
    }
    @Test fun `VPN callback without ticket stops before teaching center requests`() = runBlocking<Unit> {
        arrange()
        every { session.cookieDetails() } returns emptyList()
        assertThrows(SchoolResponseError::class.java) { runBlocking { TeachingCenterLogin(session, sso, true).authenticate("student", "password") } }
        coVerify(exactly = 0) { session.get(match { it.contains("/auth-protocol-core/login") }, any(), any()) }
    }

    private fun service(): SchoolLoginService {
        every { session.close() } just Runs
        return SchoolLoginService(emptyLogger, { session }, { _, _ -> sso })
    }

    @Test fun `teaching session expiry after identity verification returns partial success`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(landing, any(), any()) } returns
            HttpResponse(200, emptyMap(), "<html>统一身份认证</html>", landing)
        val result = service().login("student", "password", webVpn = false)
        assertEquals("教学中心会话未建立", result.teachingCenterWarning)
        assertTrue(result.cookies.isNotEmpty())
        verify(exactly = 1) { session.close() }
    }

    @Test fun `network failure after SSO does not discard verified identity`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(match { it.contains("/auth-protocol-core/callback") }, any(), any()) } throws ConnectException("offline")
        assertEquals("无法连接学校教务服务", service().login("student", "password", webVpn = false).teachingCenterWarning)
    }

    @Test fun `verified identity without a VPN cookie remains usable with warning`() = runBlocking<Unit> {
        arrange()
        every { session.cookieDetails() } returns emptyList()
        val result = service().login("student", "password", webVpn = true)
        assertNotNull(result.teachingCenterWarning)
        assertTrue(result.cookies.isEmpty())
    }

    @Test fun `teaching preflight failure verifies identity through independent known service`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(match { it.contains("/auth-protocol-core/login") }, any(), any()) } returns
            HttpResponse(200, emptyMap(), "<html>统一身份认证</html>", landing)
        val result = service().login("student", "password", webVpn = false)
        assertNotNull(result.teachingCenterWarning)
        coVerify(exactly = 1) { sso.login("student", "password", match { it.contains("webvpn.bit.edu.cn") }, true, any(), any(), any(), any()) }
    }

    @Test fun `preflight fallback still rejects incorrect credentials`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(match { it.contains("/auth-protocol-core/login") }, any(), any()) } throws ConnectException("teaching center down")
        coEvery { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) } throws IllegalArgumentException("incorrect password")
        assertThrows(IllegalArgumentException::class.java) { runBlocking { service().login("student", "wrong", webVpn = false) } }
        coVerify(exactly = 1) { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test fun `incorrect credentials are never degraded to successful login`() = runBlocking<Unit> {
        arrange()
        coEvery { sso.login(any(), any(), any(), any(), any(), any(), any(), any()) } throws IllegalArgumentException("incorrect password")
        assertThrows(IllegalArgumentException::class.java) { runBlocking { service().login("student", "wrong", webVpn = false) } }
    }

    @Test fun `wrapped cancellation after SSO never commits partial success`() = runBlocking<Unit> {
        arrange()
        coEvery { session.get(landing, any(), any()) } throws RuntimeException(CancellationException("cancel"))
        assertThrows(CancellationException::class.java) { runBlocking { service().login("student", "password", webVpn = false) } }
    }

}
