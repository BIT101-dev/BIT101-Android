package cn.bit101.android.data.repo

import cn.bit101.android.config.common.SettingItem
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.LoginRefreshResult
import cn.bit101.api.Bit101Api
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.api.model.common.SchoolLoginResult
import cn.bit101.api.model.http.bit101.PostRegisterDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyInitDataModel
import cn.bit101.bitlogin.sso.LoginError as SsoLoginError
import cn.bit101.bitlogin.sso.SmsVerificationError
import cn.bit101.bitlogin.sso.SsoHttpException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.CookieManager

class DefaultLoginRepoTest {
    private lateinit var api: Bit101Api
    private lateinit var apiManager: APIManager
    private lateinit var loginStatus: MemoryLoginStatus
    private lateinit var repo: DefaultLoginRepo

    @Before
    fun setUp() {
        api = mockk()
        apiManager = mockk()
        loginStatus = MemoryLoginStatus()
        every { apiManager.api } returns api
        coEvery { apiManager.switch(any()) } answers {
            runBlocking { loginStatus.webVpn.set(firstArg()) }
        }
        repo = DefaultLoginRepo(apiManager, loginStatus, SmsCodeRequestHub())
    }

    @Test
    fun `login saves final state only after both services succeed`() = runBlocking {
        stubSuccessfulSchoolLogin(webVpn = true)
        stubSuccessfulBackendLogin()

        assertTrue(repo.login("112233", "password"))

        assertEquals("112233", loginStatus.sid.get())
        assertEquals("password", loginStatus.password.get())
        assertEquals("fake-cookie", loginStatus.fakeCookie.get())
        assertTrue(loginStatus.status.get())
        assertTrue(loginStatus.webVpn.get())
        assertTrue(loginStatus.cookieManager.cookieStore.cookies.any { it.name == "SESSION" })
    }

    @Test
    fun `school login failure clears partial state`() {
        loginStatus.seedLoggedIn()
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws IllegalStateException("school failed")

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repo.login("112233", "password") }
        }

        assertLoggedOut()
    }

    @Test
    fun `backend login failure clears synchronized school cookies`() {
        stubSuccessfulSchoolLogin()
        coEvery { api.user.webVpnVerifyInit(any()) } throws IllegalStateException("backend failed")

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repo.login("112233", "password") }
        }

        assertLoggedOut()
    }

    @Test
    fun `valid persisted school session does not call BIT Login`() = runBlocking {
        loginStatus.seedLoggedIn()
        stubValidChecks()

        assertTrue(repo.checkLogin())

        coVerify(exactly = 0) { api.schoolLogin.login(any(), any(), any()) }
    }

    @Test
    fun `invalid school session reauthenticates with saved credentials`() = runBlocking {
        loginStatus.seedLoggedIn()
        coEvery { api.user.check() } returns Response.success(Unit)
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } throws IllegalStateException("unauthorized")
        stubSuccessfulSchoolLogin()
        stubSuccessfulBackendLogin()

        assertTrue(repo.checkLogin())

        coVerify(exactly = 1) { api.schoolLogin.login("112233", "password", any()) }
    }

    @Test
    fun `missing credentials does not attempt school login`() = runBlocking {
        coEvery { api.user.check() } returns Response.success(Unit)
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } throws IllegalStateException("unauthorized")

        assertFalse(repo.checkLogin())

        coVerify(exactly = 0) { api.schoolLogin.login(any(), any(), any()) }
    }

    @Test
    fun `operation retries at most once after a valid login check`() = runBlocking {
        loginStatus.seedLoggedIn()
        stubValidChecks()
        var calls = 0

        val result = repo.doOperationRequiresLogin {
            calls++
            if (calls == 1) throw IllegalStateException("expired request")
            "success"
        }

        assertEquals("success", result)
        assertEquals(2, calls)
    }

    @Test
    fun `logout clears credentials state and cookies`() = runBlocking {
        loginStatus.seedLoggedIn()

        repo.logout()

        assertLoggedOut()
    }

    @Test
    fun `refreshLogin refreshes school and bit101 sessions`() = runBlocking {
        loginStatus.seedLoggedIn()
        stubSuccessfulSchoolLogin(webVpn = true)
        stubSuccessfulBackendLogin()

        assertEquals(LoginRefreshResult.SUCCESS, repo.refreshLogin())

        assertTrue(loginStatus.status.get())
        assertTrue(loginStatus.webVpn.get())
        assertEquals("fake-cookie", loginStatus.fakeCookie.get())
        assertTrue(loginStatus.cookieManager.cookieStore.cookies.any { it.name == "SESSION" })
        coVerify(exactly = 1) { api.schoolLogin.login("112233", "password", any()) }
    }

    @Test
    fun `refreshLogin without saved credentials returns FAILED`() = runBlocking {
        assertEquals(LoginRefreshResult.FAILED, repo.refreshLogin())

        coVerify(exactly = 0) { api.schoolLogin.login(any(), any(), any()) }
    }

    @Test
    fun `checkLogin transient failure keeps credentials`() = runBlocking {
        loginStatus.seedLoggedIn()
        coEvery { api.user.check() } returns Response.success(Unit)
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } throws IllegalStateException("unauthorized")
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws IOException("network down")

        assertFalse(repo.checkLogin())

        assertEquals("112233", loginStatus.sid.get())
        assertEquals("password", loginStatus.password.get())
        assertEquals("fake-cookie", loginStatus.fakeCookie.get())
    }

    @Test
    fun `checkLogin sms requirement keeps credentials but marks logged out`() = runBlocking {
        loginStatus.seedLoggedIn()
        coEvery { api.user.check() } returns Response.success(Unit)
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } throws IllegalStateException("unauthorized")
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws SmsVerificationError("需要短信验证")

        assertFalse(repo.checkLogin())

        assertFalse(loginStatus.status.get())
        assertEquals("112233", loginStatus.sid.get())
        assertEquals("password", loginStatus.password.get())
        assertEquals("fake-cookie", loginStatus.fakeCookie.get())
    }

    @Test
    fun `checkLogin wrong password clears credentials`() = runBlocking {
        loginStatus.seedLoggedIn()
        coEvery { api.user.check() } returns Response.success(Unit)
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } throws IllegalStateException("unauthorized")
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws SsoLoginError("用户名或密码错误", code = "1030027")

        assertFalse(repo.checkLogin())

        assertLoggedOut()
    }

    @Test
    fun `checkLogin rate limited failure keeps credentials`() = runBlocking {
        loginStatus.seedLoggedIn()
        coEvery { api.user.check() } returns Response.success(Unit)
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } throws IllegalStateException("unauthorized")
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws SsoHttpException(429, "https://sso.bit.edu.cn", "too many requests")

        assertFalse(repo.checkLogin())

        assertEquals("112233", loginStatus.sid.get())
        assertEquals("password", loginStatus.password.get())
        assertEquals("fake-cookie", loginStatus.fakeCookie.get())
    }

    @Test
    fun `tryRefreshLogin returns BUSY while checkLogin holds the lock`() = runBlocking {
        loginStatus.seedLoggedIn()
        val checkStarted = CompletableDeferred<Unit>()
        val checkGate = CompletableDeferred<Response<Unit>>()
        coEvery { api.user.check() } coAnswers {
            checkStarted.complete(Unit)
            checkGate.await()
        }
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } returns "2025-2026-1"

        val job = async(Dispatchers.IO) { repo.checkLogin() }
        checkStarted.await()

        // 锁已被 checkLogin 持有，非阻塞刷新应立即返回 BUSY，不发起学校登录
        assertEquals(LoginRefreshResult.BUSY, repo.tryRefreshLogin())
        coVerify(exactly = 0) { api.schoolLogin.login(any(), any(), any()) }

        checkGate.complete(Response.success(Unit))
        assertTrue(job.await())
    }

    @Test
    fun `tryRefreshLogin refreshes school and bit101 sessions when idle`() = runBlocking {
        loginStatus.seedLoggedIn()
        stubSuccessfulSchoolLogin(webVpn = true)
        stubSuccessfulBackendLogin()

        assertEquals(LoginRefreshResult.SUCCESS, repo.tryRefreshLogin())

        assertTrue(loginStatus.status.get())
        assertTrue(loginStatus.webVpn.get())
        assertEquals("fake-cookie", loginStatus.fakeCookie.get())
        coVerify(exactly = 1) { api.schoolLogin.login("112233", "password", any()) }
    }

    @Test
    fun `tryRefreshLogin without saved credentials returns FAILED`() = runBlocking {
        assertEquals(LoginRefreshResult.FAILED, repo.tryRefreshLogin())

        coVerify(exactly = 0) { api.schoolLogin.login(any(), any(), any()) }
    }

    private fun stubSuccessfulSchoolLogin(webVpn: Boolean = false) {
        coEvery { api.schoolLogin.login(any(), any(), any()) } returns SchoolLoginResult(
            cookies = listOf(
                SchoolCookie(
                    name = "SESSION",
                    value = "secret",
                    domain = "sso.bit.edu.cn",
                    path = "/",
                    secure = true,
                    expiresEpochSeconds = null,
                ),
            ),
            webVpn = webVpn,
        )
    }

    private fun stubSuccessfulBackendLogin() {
        coEvery { api.user.webVpnVerifyInit(any()) } returns Response.success(
            PostWebvpnVerifyInitDataModel.Response(
                captcha = "",
                cookie = "cookie",
                execution = "execution",
                salt = "MDEyMzQ1Njc4OWFiY2RlZg==",
            ),
        )
        coEvery { api.user.webVpnVerify(any()) } returns Response.success(
            PostWebvpnVerifyDataModel.Response(token = "token", code = "code"),
        )
        coEvery { api.user.register(any()) } returns Response.success(
            PostRegisterDataModel.Response(fakeCookie = "fake-cookie"),
        )
    }

    private fun stubValidChecks() {
        coEvery { api.user.check() } returns Response.success(Unit)
        coEvery { api.schoolJxzxehall.getCurrentTerm(any(), any()) } returns "2025-2026-1"
    }

    private fun assertLoggedOut() = runBlocking {
        assertEquals("", loginStatus.sid.get())
        assertEquals("", loginStatus.password.get())
        assertEquals("", loginStatus.fakeCookie.get())
        assertFalse(loginStatus.status.get())
        assertFalse(loginStatus.webVpn.get())
        assertTrue(loginStatus.cookieManager.cookieStore.cookies.isEmpty())
    }

    private class MemoryLoginStatus : LoginStatus {
        override val sid = MemorySettingItem("")
        override val password = MemorySettingItem("")
        override val status = MemorySettingItem(false)
        override val webVpn = MemorySettingItem(false)
        override val fakeCookie = MemorySettingItem("")
        override val cookieManager = CookieManager()

        override suspend fun clear() {
            status.set(false)
            webVpn.set(false)
            sid.set("")
            password.set("")
            fakeCookie.set("")
            cookieManager.cookieStore.removeAll()
        }

        fun seedLoggedIn() = runBlocking {
            sid.set("112233")
            password.set("password")
            status.set(true)
            fakeCookie.set("fake-cookie")
        }
    }

    private class MemorySettingItem<T>(initial: T) : SettingItem<T> {
        private val state = MutableStateFlow(initial)
        override val flow = state
        override suspend fun get(): T = state.value
        override suspend fun set(value: T) {
            state.value = value
        }
    }
}
