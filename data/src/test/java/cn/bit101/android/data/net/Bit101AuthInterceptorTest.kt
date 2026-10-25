package cn.bit101.android.data.net

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.LoginRefreshResult
import cn.bit101.android.data.repo.base.LoginRepo
import mockwebserver3.MockWebServer
import mockwebserver3.MockResponse
import io.mockk.*
import okhttp3.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import javax.inject.Provider

class Bit101AuthInterceptorTest {
    @Test(timeout = 10000)
    fun `nonempty 401 is closed before retry and token header is replaced`() {
        withServer(200, LoginRefreshResult.SUCCESS) { response, headers, repo, status ->
            assertEquals(200, response.code)
            assertEquals(listOf(listOf("old"), listOf("new")), headers)
            coVerify(exactly = 1) { repo.tryRefreshLogin() }
            coVerify(exactly = 0) { status.clear() }
        }
    }

    @Test(timeout = 10000)
    fun `second 401 ends retry without deleting credentials`() {
        withServer(401, LoginRefreshResult.SUCCESS) { response, headers, repo, status ->
            assertEquals(401, response.code)
            assertEquals(2, headers.size)
            coVerify(exactly = 1) { repo.tryRefreshLogin() }
            coVerify(exactly = 0) { status.clear() }
        }
    }

    @Test(timeout = 10000)
    fun `unsuccessful refresh returns original readable response without clearing state`() {
        for (result in LoginRefreshResult.entries.filter { it != LoginRefreshResult.SUCCESS }) {
            withServer(200, result) { response, headers, _, status ->
                assertEquals(401, response.code)
                assertEquals("unauthorized", response.body.string())
                assertEquals(1, headers.size)
                coVerify(exactly = 0) { status.clear() }
            }
        }
    }

    @Test
    fun `refresh exception closes abandoned response`() {
        val status = mockk<LoginStatus>()
        val repo = mockk<LoginRepo>()
        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder().url("https://example.test/").build()
        val response = mockk<Response>(relaxed = true)
        coEvery { status.fakeCookie.get() } returns "old"
        coEvery { repo.tryRefreshLogin() } throws IllegalStateException("refresh failed")
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns response
        every { response.code } returns 401
        assertThrows(IllegalStateException::class.java) {
            Bit101AuthInterceptor(status, Provider { repo }).intercept(chain)
        }
        verify(exactly = 1) { response.close() }
    }

    private fun withServer(
        retryCode: Int,
        result: LoginRefreshResult,
        verify: (Response, List<List<String>>, LoginRepo, LoginStatus) -> Unit,
    ) {
        val status = mockk<LoginStatus>(relaxed = true)
        val repo = mockk<LoginRepo>()
        var token = "old"
        coEvery { status.fakeCookie.get() } answers { token }
        coEvery { repo.tryRefreshLogin() } answers { token = "new"; result }
        val server = MockWebServer()
        server.enqueue(MockResponse.Builder().code(401).body("unauthorized").build())
        server.enqueue(MockResponse.Builder().code(retryCode).body("retry response").build())
        server.start()
        val client = OkHttpClient.Builder().callTimeout(3, TimeUnit.SECONDS)
            .addInterceptor(Bit101AuthInterceptor(status, Provider { repo })).build()
        try {
            val request = Request.Builder().url(server.url("/"))
                .header("fake-cookie", "caller-token").build()
            client.newCall(request).execute().use {
                val headers = List(server.requestCount) {
                    server.takeRequest(1, TimeUnit.SECONDS)!!.headers.values("fake-cookie")
                }
                verify(it, headers, repo, status)
            }
        } finally {
            server.close()
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        }
    }
}
