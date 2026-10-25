package cn.bit101.android.data.repo

import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.LoginException
import cn.bit101.android.data.repo.base.LoginFailure
import cn.bit101.api.Bit101Api
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.api.model.common.SchoolLoginResult
import cn.bit101.bitlogin.sso.LoginError
import cn.bit101.bitlogin.sso.SmsVerificationError
import cn.bit101.bitlogin.sso.SsoHttpException
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DefaultSchoolLoginRepoTest {
    private lateinit var api: Bit101Api
    private lateinit var manager: APIManager
    private lateinit var status: MemoryLoginStatus
    private lateinit var repo: DefaultSchoolLoginRepo
    private val schoolResult = SchoolLoginResult(
        cookies = listOf(SchoolCookie("SESSION", "secret", "sso.bit.edu.cn", "/", true, null)),
        webVpn = true,
    )

    @Before fun setUp() {
        api = mockk()
        manager = mockk()
        status = MemoryLoginStatus()
        every { manager.api } returns api
        coEvery { manager.switch(any()) } coAnswers { status.webVpn.set(firstArg()) }
        repo = DefaultSchoolLoginRepo(manager, status, SmsCodeRequestHub(), AccountSession(status))
    }

    @Test fun `school login succeeds with no community service available`() = runBlocking {
        coEvery { api.schoolLogin.login(any(), any(), any()) } returns schoolResult
        assertTrue(repo.login("112233", "school-password"))
        assertEquals("112233", status.sid.get())
        assertTrue(status.schoolConnected.get())
        assertTrue(status.webVpn.get())
        assertEquals(listOf("SESSION"), status.cookieManager.cookieStore.cookies.map { it.name })
        assertEquals("", status.fakeCookie.get())
        verify(exactly = 0) { api.auth; api.user }
    }

    @Test fun `failed switch preserves existing school and community sessions`() = runBlocking {
        status.seedSchool()
        status.fakeCookie.set("community-token")
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws IOException("offline")
        assertThrows(LoginException::class.java) { runBlocking { repo.login("other", "password") } }
        assertEquals("112233", status.sid.get())
        assertEquals("school-password", status.password.get())
        assertEquals("community-token", status.fakeCookie.get())
        assertEquals(listOf("OLD"), status.cookieManager.cookieStore.cookies.map { it.name })
        assertTrue(status.schoolConnected.get())
    }

    @Test fun `same school account preserves verified community password`() = runBlocking {
        status.seedSchool()
        status.fakeCookie.set("community-token")
        status.communityPasswordDigest.set("independent-digest")
        coEvery { api.schoolLogin.login(any(), any(), any()) } returns schoolResult
        repo.login("112233", "new-school-password")
        assertEquals("independent-digest", status.communityPasswordDigest.get())
        assertEquals("community-token", status.fakeCookie.get())
    }

    @Test fun `successful account switch clears previous community identity`() = runBlocking {
        status.seedSchool()
        status.fakeCookie.set("community-token")
        status.communityPasswordDigest.set("old-digest")
        coEvery { api.schoolLogin.login(any(), any(), any()) } returns schoolResult
        repo.login("other", "new-password")
        assertEquals("other", status.sid.get())
        assertEquals("", status.fakeCookie.get())
        assertEquals("", status.communityPasswordDigest.get())
        assertEquals(listOf("SESSION"), status.cookieManager.cookieStore.cookies.map { it.name })
    }

    @Test fun `ordinary school requests never probe authentication services`() = runBlocking {
        status.seedSchool()
        assertEquals("result", repo.withSchoolSession { "result" })
        verify { api wasNot Called }
    }

    @Test fun `generic network and parsing failures never trigger login`() = runBlocking {
        status.seedSchool()
        for (error in listOf(IOException("offline"), IllegalStateException("invalid JSON"), SsoHttpException(503, "https://sso.bit.edu.cn", "unavailable"))) {
            assertThrows(error.javaClass) { runBlocking { repo.withSchoolSession { throw error } } }
        }
        verify { api wasNot Called }
    }

    @Test fun `explicit 401 restores only school and retries at most once`() = runBlocking {
        status.seedSchool()
        coEvery { api.schoolLogin.login(any(), any(), any()) } returns schoolResult
        var attempts = 0
        assertThrows(SsoHttpException::class.java) { runBlocking {
            repo.withSchoolSession { attempts++; throw SsoHttpException(401, "https://sso.bit.edu.cn", "expired") }
        } }
        assertEquals(2, attempts)
        coVerify(exactly = 1) { api.schoolLogin.login("112233", "school-password", any()) }
        verify(exactly = 0) { api.auth; api.user }
    }

    @Test fun `transient reconnect failure preserves credentials and connection flag`() = runBlocking {
        status.seedSchool()
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws IOException("offline")
        val error = assertThrows(LoginException::class.java) { runBlocking { repo.reconnect() } }
        assertEquals(LoginFailure.TRANSIENT, error.result)
        assertTrue(status.schoolConnected.get())
        assertEquals("school-password", status.password.get())
    }

    @Test fun `interactive or credential failure marks school only and retains cached access credentials`() = runBlocking {
        for (error in listOf(SmsVerificationError("sms"), LoginError("bad password", code = "1030027"))) {
            status.seedSchool()
            status.fakeCookie.set("community-token")
            coEvery { api.schoolLogin.login(any(), any(), any()) } throws error
            assertThrows(LoginException::class.java) { runBlocking { repo.reconnect() } }
            assertFalse(status.schoolConnected.get())
            assertEquals("school-password", status.password.get())
            assertEquals("community-token", status.fakeCookie.get())
        }
    }

    @Test(timeout = 10000) fun `logout prevents late school login from restoring account`() = runBlocking {
        status.seedSchool()
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        coEvery { api.schoolLogin.login(any(), any(), any()) } coAnswers {
            started.complete(Unit); gate.await(); schoolResult
        }
        val job = async { repo.login("other", "password") }
        started.await()
        repo.logout()
        gate.complete(Unit)
        assertThrows(CancellationException::class.java) { runBlocking { job.await() } }
        assertEquals("", status.sid.get())
        assertTrue(status.cookieManager.cookieStore.cookies.isEmpty())
    }

    @Test fun `wrapped cancellation is propagated without changing credentials`() = runBlocking {
        status.seedSchool()
        coEvery { api.schoolLogin.login(any(), any(), any()) } throws RuntimeException(CancellationException("cancel"))
        assertThrows(CancellationException::class.java) { runBlocking { repo.reconnect() } }
        assertTrue(status.schoolConnected.get())
    }
}
