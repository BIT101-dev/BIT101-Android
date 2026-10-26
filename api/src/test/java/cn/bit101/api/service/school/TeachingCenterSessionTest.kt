package cn.bit101.api.service.school

import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.http.CookieDetail
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.http.HttpResponse
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.net.UnknownHostException

class TeachingCenterSessionTest {
    private val cookie = SchoolCookie("SESSION", "old", "jxzxehallapp.bit.edu.cn", "/", true, null)
    private val client = mockk<HttpClient>(relaxed = true).also { client ->
        every { client.headers } returns mutableMapOf()
        every { client.cookieDetails() } returns listOf(CookieDetail("SESSION", "new", cookie.domain, "/", true, null))
        coEvery { client.get(any(), any(), any()) } coAnswers { HttpResponse(200, emptyMap(), "{}", firstArg()) }
    }
    private val sessions = TeachingCenterSession { client }

    @Test fun `classroom apps share common warmup before their own initialization`() = runBlocking<Unit> {
        sessions.use(listOf(cookie), false, "kxjasbyMobile") { it.json("/campuses") }
        repeat(2) { sessions.use(listOf(cookie), false, "kxjas") { it.json("/rooms") } }
        coVerifyOrder {
            client.get(match { it.contains("/wdkbby/*default/index.do") }, any(), any())
            client.get(match { it.contains("getAppConfig/wdkbby-") }, any(), any())
            client.get(match { it.contains("/i18n.do") }, any(), any())
            client.get(match { it.contains("/kxjasbyMobile/*default/index.do") }, any(), any())
            client.get(match { it.endsWith("/campuses") }, any(), any())
            client.get(match { it.contains("/kxjas/*default/index.do") }, any(), any())
            client.get(match { it.endsWith("/rooms") }, any(), any())
        }
        coVerify(exactly = 1) { client.get(match { it.contains("getAppConfig/wdkbby-") }, any(), any()) }
        coVerify(exactly = 1) { client.get(match { it.contains("/kxjas/*default/index.do") }, any(), any()) }
    }

    @Test fun `warmup runs once and business cookies survive client close`() = runBlocking<Unit> {
        repeat(2) { sessions.use(listOf(cookie), false) { it.json("/data") } }
        coVerify(exactly = 1) { client.get(match { it.contains("getAppConfig/wdkbby-") }, any(), any()) }
        coVerify(exactly = 1) { client.get(match { it.contains("/i18n.do") }, any(), any()) }
        coVerify { client.addCookie("SESSION", "new", cookie.domain, "/", true, null) }
        verify(exactly = 2) { client.close() }
    }

    @Test fun `new credentials and route changes repeat warmup without old cookie leakage`() = runBlocking<Unit> {
        sessions.use(listOf(cookie), false) { }
        sessions.use(listOf(cookie.copy(value = "another-account")), true) { }
        coVerify(exactly = 2) { client.get(match { it.contains("getAppConfig/wdkbby-") }, any(), any()) }
        coVerify { client.get(match { it.startsWith("https://webvpn.bit.edu.cn/") }, any(), any()) }
        coVerify(exactly = 0) { client.addCookie("SESSION", "new", any(), any(), any(), any()) }
    }

    @Test fun `failed warmup does not run business request or remain prepared`() = runBlocking<Unit> {
        coEvery { client.get(match { it.contains("getAppConfig") }, any(), any()) } throws SchoolSessionExpired()
        assertThrows(SchoolSessionExpired::class.java) { runBlocking { sessions.use(listOf(cookie), false) { it.json("/data") } } }
        coVerify(exactly = 0) { client.get(match { it.endsWith("/data") }, any(), any()) }
        coEvery { client.get(match { it.contains("getAppConfig") }, any(), any()) } coAnswers { HttpResponse(200, emptyMap(), "{}", firstArg()) }
        sessions.use(listOf(cookie), false) { it.json("/data") }
        coVerify(exactly = 2) { client.get(match { it.contains("getAppConfig") }, any(), any()) }
    }

    @Test fun `only connection errors request a route change`() = runBlocking<Unit> {
        coEvery { client.get(any(), any(), any()) } throws UnknownHostException()
        val error = assertThrows(SchoolRouteUnavailable::class.java) { runBlocking { sessions.use(listOf(cookie), false) { } } }
        assertFalse(error.webVpn)
        coEvery { client.get(any(), any(), any()) } returns HttpResponse(503, emptyMap(), "down", "https://jxzxehallapp.bit.edu.cn/")
        assertThrows(SchoolResponseError::class.java) { runBlocking { sessions.use(listOf(cookie), false) { } } }
    }
    @Test fun `warmup accepts non-object configuration content while business APIs still require JSON`() = runBlocking<Unit> {
        coEvery { client.get(match { it.contains("getAppConfig") }, any(), any()) } coAnswers {
            HttpResponse(200, emptyMap(), "window.appConfig = {};", firstArg())
        }
        sessions.use(listOf(cookie), false) { it.json("/data") }
        coVerify(exactly = 1) { client.get(match { it.endsWith("/data") }, any(), any()) }
        coEvery { client.get(match { it.endsWith("/data") }, any(), any()) } coAnswers {
            HttpResponse(200, emptyMap(), "<html>gateway error</html>", firstArg())
        }
        assertThrows(SchoolResponseError::class.java) { runBlocking { sessions.use(listOf(cookie), false) { it.json("/data") } } }
    }

    @Test fun `warmup still rejects a configuration login page`() = runBlocking<Unit> {
        coEvery { client.get(match { it.contains("getAppConfig") }, any(), any()) } coAnswers {
            HttpResponse(200, emptyMap(), "<html>统一身份认证</html>", firstArg())
        }
        assertThrows(SchoolSessionExpired::class.java) { runBlocking { sessions.use(listOf(cookie), false) { it.json("/data") } } }
        coVerify(exactly = 0) { client.get(match { it.endsWith("/data") }, any(), any()) }
    }

}
