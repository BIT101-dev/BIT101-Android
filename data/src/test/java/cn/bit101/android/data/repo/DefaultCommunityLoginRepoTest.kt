package cn.bit101.android.data.repo

import cn.bit101.android.data.common.HashUtils
import cn.bit101.android.data.net.NetworkStatus
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.LoginException
import cn.bit101.api.Bit101Api
import cn.bit101.api.service.bit101.AuthApiService
import cn.bit101.api.model.http.bit101.*
import cn.bit101.android.data.repo.base.LoginFailure
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class DefaultCommunityLoginRepoTest {
    private lateinit var auth: AuthApiService
    private lateinit var api: Bit101Api
    private lateinit var manager: APIManager
    private lateinit var status: MemoryLoginStatus
    private lateinit var account: AccountSession
    private lateinit var clock: SessionClock
    private lateinit var network: NetworkStatus
    private lateinit var repo: DefaultCommunityLoginRepo
    private var now = 1_800_000_000_000L

    @Before fun setUp() = runBlocking {
        api = mockk()
        auth = mockk()
        every { api.auth } returns auth
        manager = mockk()
        status = MemoryLoginStatus()
        status.seedSchool()
        account = AccountSession(status, io.mockk.mockk(relaxed = true))
        clock = mockk()
        network = mockk()
        every { manager.api } returns api
        every { clock.now() } answers { now }
        every { network.isOnline() } returns true
        repo = newRepo()
    }

    private fun newRepo() = DefaultCommunityLoginRepo(manager, status, account, clock, network)
    private fun success(token: String = "new-token") = Response.success(PostLoginDataModel.Response(token))
    private fun fail(code: Int = 500, retryAfter: String? = null): Response<PostLoginDataModel.Response> {
        val raw = okhttp3.Response.Builder().request(okhttp3.Request.Builder().url("https://example.test").build())
            .protocol(okhttp3.Protocol.HTTP_1_1).code(code).message("error")
        if (retryAfter != null) raw.header("Retry-After", retryAfter)
        return Response.error("failure".toResponseBody(), raw.build())
    }

    private suspend fun assertSchoolIntact() {
        assertEquals("112233", status.sid.get())
        assertEquals("school-password", status.password.get())
        assertTrue(status.schoolConnected.get())
        assertEquals(listOf("OLD"), status.cookieManager.cookieStore.cookies.map { it.name })
        verify(exactly = 0) { api.schoolLogin; api.user }
    }

    @Test fun `existing token is reused without authentication traffic`() = runBlocking {
        status.fakeCookie.set("legacy-token")
        repeat(10) { assertTrue(repo.ensureConnected()) }
        verify { api wasNot Called }
    }

    @Test fun `automatic login uses only login endpoint and saves verified digest`() = runBlocking {
        coEvery { auth.login(any()) } returns success()
        assertTrue(repo.ensureConnected())
        coVerify(exactly = 1) { auth.login(PostLoginDataModel.Body("112233", HashUtils.md5("school-password"))) }
        assertEquals(HashUtils.md5("school-password"), status.communityPasswordDigest.get())
        assertEquals("new-token", status.fakeCookie.get())
        assertSchoolIntact()
    }

    @Test fun `server errors preserve school and persist backoff across repository recreation`() = runBlocking {
        coEvery { auth.login(any()) } answers { fail() }
        for (delay in listOf(1_800_000L, 7_200_000L, 86_400_000L, 86_400_000L)) {
            assertFalse(repo.ensureConnected())
            assertEquals(now + delay, status.communityRetryAt.get())
            assertFalse(status.communityNeedsLogin.get())
            repo = newRepo()
            assertFalse(repo.ensureConnected())
            now += delay
        }
        coVerify(exactly = 4) { auth.login(any()) }
        assertSchoolIntact()
    }

    @Test fun `retry after is honored and forced retry can bypass cooldown`() = runBlocking {
        coEvery { auth.login(any()) } answers { fail(429, "3600") }
        assertFalse(repo.ensureConnected())
        assertEquals(now + 3_600_000, status.communityRetryAt.get())
        coEvery { auth.login(any()) } returns success()
        assertTrue(repo.ensureConnected(force = true))
        assertEquals(0L, status.communityRetryAt.get())
        assertEquals(0, status.communityAttempts.get())
    }

    @Test fun `explicit credential rejection pauses automatic login until manual success`() = runBlocking {
        coEvery { auth.login(any()) } answers { fail(401) }
        assertFalse(repo.ensureConnected())
        assertTrue(status.communityNeedsLogin.get())
        now += 86_400_000
        assertFalse(repo.ensureConnected())
        coVerify(exactly = 1) { auth.login(any()) }
        coEvery { auth.login(any()) } returns success()
        repo.login("112233", "community-password")
        assertFalse(status.communityNeedsLogin.get())
        assertEquals(HashUtils.md5("community-password"), status.communityPasswordDigest.get())
        assertSchoolIntact()
    }

    @Test fun `verified independent community password survives school password changes`() = runBlocking {
        status.communityPasswordDigest.set("verified-digest")
        status.password.set("changed-school-password")
        coEvery { auth.login(any()) } returns success()
        assertTrue(repo.ensureConnected())
        coVerify { auth.login(PostLoginDataModel.Body("112233", "verified-digest")) }
        assertEquals("changed-school-password", status.password.get())
    }

    @Test fun `failed manual password does not overwrite verified digest or usable token`() = runBlocking {
        status.communityPasswordDigest.set("verified-digest")
        status.fakeCookie.set("usable-token")
        coEvery { auth.login(any()) } answers { fail(500) }
        assertThrows(LoginException::class.java) { runBlocking { repo.login("112233", "wrong-password") } }
        assertEquals("verified-digest", status.communityPasswordDigest.get())
        assertEquals("usable-token", status.fakeCookie.get())
        assertSchoolIntact()
    }

    @Test fun `community only login does not require or save school credentials`() = runBlocking {
        status.clear()
        coEvery { auth.login(any()) } returns success()
        repo.login("112233", "community-password")
        assertEquals("112233", status.sid.get())
        assertEquals("", status.password.get())
        assertFalse(status.schoolConnected.get())
        assertEquals("new-token", status.fakeCookie.get())
        assertTrue(status.cookieManager.cookieStore.cookies.isEmpty())
        verify(exactly = 0) { api.schoolLogin; api.user }
    }

    @Test fun `community account cannot silently replace school identity`() = runBlocking {
        assertThrows(LoginException::class.java) { runBlocking { repo.login("other", "password") } }
        verify { api wasNot Called }
        assertSchoolIntact()
    }

    @Test fun `401 discards only rejected token and attempts community renewal`() = runBlocking {
        status.fakeCookie.set("old-token")
        coEvery { auth.login(any()) } returns success()
        assertEquals("new-token", repo.refresh("112233", "old-token"))
        assertEquals("new-token", repo.refresh("112233", "old-token"))
        coVerify(exactly = 1) { auth.login(any()) }
        assertSchoolIntact()
    }

    @Test fun `failed 401 renewal retains school and subsequent requests respect cooldown`() = runBlocking {
        status.fakeCookie.set("old-token")
        coEvery { auth.login(any()) } answers { fail() }
        assertNull(repo.refresh("112233", "old-token"))
        repeat(5) { assertNull(repo.refresh("112233", "")) }
        coVerify(exactly = 1) { auth.login(any()) }
        assertEquals("", status.fakeCookie.get())
        assertSchoolIntact()
    }

    @Test(timeout = 10000) fun `concurrent forced requests share one attempt`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        coEvery { auth.login(any()) } coAnswers { started.complete(Unit); gate.await(); success() }
        val first = async { repo.ensureConnected(force = true) }
        started.await()
        val waiting = List(10) { async(Dispatchers.IO) { repo.ensureConnected(force = true) } }
        delay(150)
        gate.complete(Unit)
        assertTrue(first.await())
        assertTrue(waiting.awaitAll().all { it })
        coVerify(exactly = 1) { auth.login(any()) }
    }

    @Test(timeout = 10000) fun `manual password submission is honored after automatic attempt`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        coEvery { auth.login(any()) } coAnswers {
            if (firstArg<PostLoginDataModel.Body>().password == HashUtils.md5("school-password")) {
                started.complete(Unit); gate.await(); fail()
            } else success()
        }
        val automatic = async { repo.ensureConnected() }
        started.await()
        val manual = async { repo.login("112233", "community-password") }
        delay(100)
        gate.complete(Unit)
        assertFalse(automatic.await())
        manual.await()
        assertEquals(HashUtils.md5("community-password"), status.communityPasswordDigest.get())
        coVerify(exactly = 2) { auth.login(any()) }
    }

    @Test(timeout = 10000) fun `cancelled attempt retains cooldown and never overwrites credentials`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        coEvery { auth.login(any()) } coAnswers { started.complete(Unit); awaitCancellation() }
        val job = launch { repo.ensureConnected() }
        started.await()
        job.cancelAndJoin()
        assertFalse(newRepo().ensureConnected())
        assertTrue(status.communityRetryAt.get() > now)
        assertSchoolIntact()
        assertFalse(repo.state.first().connecting)
        coVerify(exactly = 1) { auth.login(any()) }
    }

    @Test(timeout = 10000) fun `logout or switching account discards delayed community response`() = runBlocking {
        for (switch in listOf(false, true)) {
            setUp()
            val started = CompletableDeferred<Unit>()
            val gate = CompletableDeferred<Unit>()
            coEvery { auth.login(any()) } coAnswers { started.complete(Unit); gate.await(); success() }
            val job = async { repo.ensureConnected() }
            started.await()
            if (switch) account.commit(account.identity(), "other") { status.password.set("other-password") }
            else account.logout()
            gate.complete(Unit)
            assertThrows(CancellationException::class.java) { runBlocking { job.await() } }
            assertEquals(if (switch) "other" else "", status.sid.get())
            assertEquals("", status.fakeCookie.get())
        }
    }

    @Test(timeout = 10000) fun `offline foreground never starts authentication and stops on cancellation`() = runBlocking {
        val checked = CompletableDeferred<Unit>()
        every { network.isOnline() } answers { checked.complete(Unit); false }
        val job = launch { repo.autoLogin() }
        checked.await()
        job.cancelAndJoin()
        verify { api wasNot Called }
    }

    @Test(timeout = 10000) fun `new school identity starts foreground login without restarting app`() = runBlocking {
        status.clear()
        val started = CompletableDeferred<Unit>()
        coEvery { auth.login(any()) } coAnswers { started.complete(Unit); success() }
        val job = launch { repo.autoLogin() }
        yield()
        account.commit(account.identity(), "112233") { status.password.set("school-password") }
        started.await()
        withTimeout(3000) { status.fakeCookie.flow.first { it == "new-token" } }
        job.cancelAndJoin()
        coVerify(exactly = 1) { auth.login(any()) }
    }
    private fun stubIdentityLogin(captcha: String = "") {
        coEvery { auth.webVpnVerifyInit(any()) } returns Response.success(
            PostWebvpnVerifyInitDataModel.Response(captcha, "cookie", "execution", "MDEyMzQ1Njc4OWFiY2RlZg=="),
        )
        coEvery { auth.webVpnVerify(any()) } returns Response.success(PostWebvpnVerifyDataModel.Response("proof", "123456"))
        coEvery { auth.register(any()) } returns Response.success(PostRegisterDataModel.Response("identity-token"))
    }

    @Test fun `identity login uses registration login mode and preserves independent community password`() = runBlocking {
        status.communityPasswordDigest.set("verified-community-password")
        stubIdentityLogin()
        repo.loginWithSchool("112233", "school-password")
        coVerifyOrder {
            auth.webVpnVerifyInit(PostWebvpnVerifyInitDataModel.Body("112233"))
            auth.webVpnVerify(any())
            auth.register(PostRegisterDataModel.Body(HashUtils.md5("school-password"), "proof", "123456", loginMode = true))
        }
        val encrypted = slot<PostWebvpnVerifyDataModel.Body>()
        coVerify { auth.webVpnVerify(capture(encrypted)) }
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec("0123456789abcdef".toByteArray(), "AES"))
        assertEquals("school-password", String(cipher.doFinal(Base64.getDecoder().decode(encrypted.captured.password))))
        assertEquals("identity-token", status.fakeCookie.get())
        assertEquals("verified-community-password", status.communityPasswordDigest.get())
        coVerify(exactly = 0) { auth.login(any()); auth.mailVerify(any()) }
        assertSchoolIntact()
    }

    @Test fun `first identity login allows backend account creation without inventing a verified community password`() = runBlocking {
        status.clear()
        stubIdentityLogin()
        repo.loginWithSchool("112233", "school-password")
        assertEquals("112233", status.sid.get())
        assertEquals("identity-token", status.fakeCookie.get())
        assertEquals("", status.communityPasswordDigest.get())
        assertEquals("", status.password.get())
        assertFalse(status.schoolConnected.get())
        coVerify { auth.register(match { it.loginMode == true }) }
    }

    @Test fun `campus login reuses usable community token without server school verification`() = runBlocking {
        status.fakeCookie.set("usable-token")
        repo.loginWithSchool("112233", "school-password", force = false)
        verify { api wasNot Called }
    }

    @Test fun `failed identity verification at every stage preserves campus and never triggers automatic webvpn retry`() = runBlocking {
        for (stage in listOf("init", "verify", "register")) {
            setUp()
            stubIdentityLogin()
            when (stage) {
                "init" -> coEvery { auth.webVpnVerifyInit(any()) } returns Response.error(503, "failed".toResponseBody())
                "verify" -> coEvery { auth.webVpnVerify(any()) } returns Response.error(503, "failed".toResponseBody())
                else -> coEvery { auth.register(any()) } returns Response.error(503, "failed".toResponseBody())
            }
            assertThrows(LoginException::class.java) { runBlocking { repo.loginWithSchool("112233", "school-password") } }
            assertSchoolIntact()
            assertEquals("", status.fakeCookie.get())
            assertFalse(repo.ensureConnected())
            now = status.communityRetryAt.get()
            coEvery { auth.login(any()) } answers { fail() }
            assertFalse(repo.ensureConnected())
            coVerify(exactly = 1) { auth.webVpnVerifyInit(any()) }
            coVerify(exactly = 1) { auth.login(any()) }
            if (stage == "init") coVerify(exactly = 0) { auth.webVpnVerify(any()); auth.register(any()) }
            if (stage == "verify") coVerify(exactly = 0) { auth.register(any()) }
        }
    }

    @Test fun `captcha requirement stops before verify or registration`() = runBlocking {
        stubIdentityLogin(captcha = "image")
        val error = assertThrows(LoginException::class.java) { runBlocking { repo.loginWithSchool("112233", "password") } }
        assertEquals(LoginFailure.NEEDS_INTERACTIVE, error.result)
        coVerify(exactly = 0) { auth.webVpnVerify(any()); auth.register(any()) }
        assertSchoolIntact()
    }

    @Test(timeout = 10000) fun `logout while identity registration is pending discards returned token`() = runBlocking {
        stubIdentityLogin()
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        coEvery { auth.register(any()) } coAnswers {
            started.complete(Unit); gate.await(); Response.success(PostRegisterDataModel.Response("late-token"))
        }
        val job = async { repo.loginWithSchool("112233", "password") }
        started.await()
        account.logout()
        gate.complete(Unit)
        assertThrows(CancellationException::class.java) { runBlocking { job.await() } }
        assertEquals("", status.sid.get())
        assertEquals("", status.fakeCookie.get())
    }

    @Test(timeout = 10000) fun `account change after identity initialization prevents verification and registration`() = runBlocking {
        stubIdentityLogin()
        val started = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        coEvery { auth.webVpnVerifyInit(any()) } coAnswers {
            started.complete(Unit); gate.await()
            Response.success(PostWebvpnVerifyInitDataModel.Response("", "cookie", "execution", "MDEyMzQ1Njc4OWFiY2RlZg=="))
        }
        val job = async { repo.loginWithSchool("112233", "password") }
        started.await()
        account.commit(account.identity(), "other") { status.password.set("other-password") }
        gate.complete(Unit)
        assertThrows(CancellationException::class.java) { runBlocking { job.await() } }
        coVerify(exactly = 0) { auth.webVpnVerify(any()); auth.register(any()) }
        assertEquals("other", status.sid.get())
        assertEquals("", status.fakeCookie.get())
    }

    private fun expiringToken(exp: Long = now / 1000) = "header." + Base64.getUrlEncoder().withoutPadding()
        .encodeToString("{\"exp\":$exp}".toByteArray()) + ".signature"

    @Test fun `known token expiry renews with school identity without requiring equal passwords`() = runBlocking {
        status.fakeCookie.set(expiringToken())
        status.communityPasswordDigest.set("different-community-password")
        stubIdentityLogin()
        assertTrue(repo.ensureConnected())
        assertEquals("identity-token", status.fakeCookie.get())
        assertEquals("different-community-password", status.communityPasswordDigest.get())
        assertFalse(status.communityRenewalPending.get())
        assertEquals(now + 86_400_000, status.communityIdentityRetryAt.get())
        coVerify(exactly = 0) { auth.login(any()) }
        coVerify(exactly = 1) { auth.webVpnVerifyInit(any()); auth.webVpnVerify(any()); auth.register(any()) }
        assertSchoolIntact()
    }

    @Test fun `valid token never triggers identity renewal until last minute`() = runBlocking {
        status.fakeCookie.set(expiringToken(now / 1000 + 61))
        assertTrue(repo.ensureConnected())
        verify { auth wasNot Called }
        now += 1000
        stubIdentityLogin()
        assertTrue(repo.ensureConnected())
        coVerify(exactly = 1) { auth.webVpnVerifyInit(any()) }
    }

    @Test fun `failed automatic identity renewal waits a full day across restart and 401 token removal`() = runBlocking {
        val expired = expiringToken()
        status.fakeCookie.set(expired)
        coEvery { auth.webVpnVerifyInit(any()) } returns Response.error(503, "unavailable".toResponseBody())
        assertNull(repo.refresh("112233", expired))
        assertEquals("", status.fakeCookie.get())
        assertTrue(status.communityRenewalPending.get())
        assertEquals(now + 86_400_000, status.communityIdentityRetryAt.get())
        repo = newRepo()
        now += 86_400_000 - 1
        repeat(10) { assertFalse(repo.ensureConnected()) }
        coVerify(exactly = 1) { auth.webVpnVerifyInit(any()) }
        coVerify(exactly = 0) { auth.login(any()) }
        now++
        stubIdentityLogin()
        assertTrue(repo.ensureConnected())
        coVerify(exactly = 2) { auth.webVpnVerifyInit(any()) }
        assertFalse(status.communityRenewalPending.get())
        assertSchoolIntact()
    }

    @Test fun `identity retry after can extend the daily limit`() = runBlocking {
        status.fakeCookie.set(expiringToken())
        val response = okhttp3.Response.Builder().request(okhttp3.Request.Builder().url("https://example.test").build())
            .protocol(okhttp3.Protocol.HTTP_1_1).code(429).message("limited").header("Retry-After", "172800").build()
        coEvery { auth.webVpnVerifyInit(any()) } returns Response.error("limited".toResponseBody(), response)
        assertFalse(repo.ensureConnected())
        assertEquals(now + 172_800_000, status.communityIdentityRetryAt.get())
        now += 86_400_000
        assertFalse(repo.ensureConnected())
        coVerify(exactly = 1) { auth.webVpnVerifyInit(any()) }
    }

    @Test fun `unknown token 401 and missing token only use ordinary password login`() = runBlocking {
        coEvery { auth.login(any()) } returns success()
        assertTrue(repo.ensureConnected())
        status.fakeCookie.set("opaque-token")
        assertEquals("new-token", repo.refresh("112233", "opaque-token"))
        coVerify(exactly = 2) { auth.login(any()) }
        coVerify(exactly = 0) { auth.webVpnVerifyInit(any()); auth.webVpnVerify(any()); auth.register(any()) }
    }

    @Test fun `unexpired token rejected by server does not trigger webvpn`() = runBlocking {
        val token = expiringToken(now / 1000 + 86_400)
        status.fakeCookie.set(token)
        coEvery { auth.login(any()) } returns success()
        assertEquals("new-token", repo.refresh("112233", token))
        coVerify(exactly = 0) { auth.webVpnVerifyInit(any()) }
    }

    @Test fun `expiry without saved school password uses verified community password`() = runBlocking {
        status.password.set("")
        status.communityPasswordDigest.set("verified-digest")
        status.fakeCookie.set(expiringToken())
        coEvery { auth.login(any()) } returns success()
        assertTrue(repo.ensureConnected())
        coVerify(exactly = 1) { auth.login(PostLoginDataModel.Body("112233", "verified-digest")) }
        coVerify(exactly = 0) { auth.webVpnVerifyInit(any()) }
    }

    @Test fun `short lived token from successful identity login cannot cause repeated automatic webvpn requests`() = runBlocking {
        status.fakeCookie.set(expiringToken())
        stubIdentityLogin()
        coEvery { auth.register(any()) } returns Response.success(PostRegisterDataModel.Response(expiringToken(now / 1000 + 120)))
        assertTrue(repo.ensureConnected())
        now += 60_000
        repeat(5) { assertFalse(newRepo().ensureConnected()) }
        coVerify(exactly = 1) { auth.webVpnVerifyInit(any()) }
        coVerify(exactly = 0) { auth.login(any()) }
    }

    @Test fun `automatic identity captcha pauses until user acts while manual identity can bypass cooldown`() = runBlocking {
        status.fakeCookie.set(expiringToken())
        stubIdentityLogin(captcha = "image")
        assertFalse(repo.ensureConnected())
        assertTrue(status.communityNeedsLogin.get())
        now += 2 * 86_400_000
        assertFalse(repo.ensureConnected())
        coVerify(exactly = 1) { auth.webVpnVerifyInit(any()) }
        stubIdentityLogin()
        repo.loginWithSchool("112233", "school-password")
        assertFalse(status.communityNeedsLogin.get())
        assertFalse(status.communityRenewalPending.get())
        assertSchoolIntact()
    }

    @Test(timeout = 10000) fun `cancelled automatic identity renewal preserves daily cooldown`() = runBlocking {
        status.fakeCookie.set(expiringToken())
        val started = CompletableDeferred<Unit>()
        coEvery { auth.webVpnVerifyInit(any()) } coAnswers { started.complete(Unit); awaitCancellation() }
        val job = launch { repo.ensureConnected() }
        started.await()
        job.cancelAndJoin()
        assertFalse(newRepo().ensureConnected())
        assertTrue(status.communityRenewalPending.get())
        assertEquals(now + 86_400_000, status.communityIdentityRetryAt.get())
        coVerify(exactly = 1) { auth.webVpnVerifyInit(any()) }
        assertSchoolIntact()
    }

    @Test fun `identity failures back off one two four eight days independently of ordinary failures and persist`() = runBlocking {
        status.fakeCookie.set(expiringToken())
        status.communityAttempts.set(3)
        coEvery { auth.webVpnVerifyInit(any()) } returns Response.error(503, "offline".toResponseBody())
        for ((index, days) in listOf(1L, 2L, 4L, 8L, 8L).withIndex()) {
            assertFalse(repo.ensureConnected())
            val next = now + days * 86_400_000
            assertEquals(next, status.communityIdentityRetryAt.get())
            assertEquals((index + 1).coerceAtMost(4), status.communityIdentityAttempts.get())
            assertEquals(3, status.communityAttempts.get())
            repo = newRepo()
            now = next - 1
            assertFalse(repo.ensureConnected())
            coVerify(exactly = index + 1) { auth.webVpnVerifyInit(any()) }
            now = next
        }
        coVerify(exactly = 0) { auth.login(any()) }
        assertSchoolIntact()
    }

    @Test fun `successful identity login resets failure backoff but preserves minimum renewal interval`() = runBlocking {
        status.fakeCookie.set(expiringToken())
        status.communityIdentityAttempts.set(4)
        status.communityIdentityRetryAt.set(now + 8 * 86_400_000)
        stubIdentityLogin()
        repo.loginWithSchool("112233", "school-password")
        assertEquals(0, status.communityIdentityAttempts.get())
        assertEquals(now + 86_400_000, status.communityIdentityRetryAt.get())
        status.fakeCookie.set(expiringToken())
        assertFalse(repo.ensureConnected())
        now += 86_400_000
        coEvery { auth.webVpnVerifyInit(any()) } returns Response.error(503, "offline".toResponseBody())
        assertFalse(repo.ensureConnected())
        assertEquals(now + 86_400_000, status.communityIdentityRetryAt.get())
        assertEquals(1, status.communityIdentityAttempts.get())
    }

    @Test fun `successful ordinary login resets identity failure history and logout clears it`() = runBlocking {
        status.communityIdentityAttempts.set(4)
        status.communityIdentityRetryAt.set(now + 8 * 86_400_000)
        status.communityRenewalPending.set(true)
        coEvery { auth.login(any()) } returns success()
        repo.login("112233", "community-password")
        assertEquals(0, status.communityIdentityAttempts.get())
        assertFalse(status.communityRenewalPending.get())
        assertEquals(now + 86_400_000, status.communityIdentityRetryAt.get())
        account.logout()
        assertEquals(0, status.communityIdentityAttempts.get())
        assertEquals(0L, status.communityIdentityRetryAt.get())
    }

}
