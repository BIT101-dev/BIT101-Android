package cn.bit101.android.data.net

import cn.bit101.android.data.repo.*
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.api.Bit101ApiFactory
import cn.bit101.api.option.DEFAULT_API_OPTION
import io.mockk.*
import kotlinx.coroutines.*
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import javax.inject.Provider

class AuthDispatcherTest {
    @Test(timeout = 10000) fun `renewal completes while business dispatcher is fully occupied by 401`() = runBlocking {
        val status = MemoryLoginStatus()
        status.seedSchool()
        status.fakeCookie.set("old")
        val manager = mockk<APIManager>()
        val network = mockk<NetworkStatus>()
        val repo = DefaultCommunityLoginRepo(manager, status, AccountSession(status), SessionClock(), network)
        val dispatcher = Dispatcher().apply { maxRequests = 1; maxRequestsPerHost = 1 }
        val client = OkHttpClient.Builder().dispatcher(dispatcher).callTimeout(4, TimeUnit.SECONDS)
            .addInterceptor(Bit101AuthInterceptor(status, Provider { repo })).build()
        val server = MockWebServer()
        server.dispatcher = object : mockwebserver3.Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.url.encodedPath == "/user/login" -> MockResponse.Builder().body("{\"fake_cookie\":\"new\"}").build()
                request.headers["fake-cookie"] == "new" -> MockResponse.Builder().body("{}").build()
                else -> MockResponse.Builder().code(401).body("expired").build()
            }
        }
        server.start()
        try {
            val urls = DEFAULT_API_OPTION.localUrls.copy(bit101Url = server.url("/").toString())
            val api = Bit101ApiFactory.create(DEFAULT_API_OPTION.copy(bit101Client = client, localUrls = urls))
            every { manager.api } returns api
            assertTrue(withTimeout(5000) { api.user.check().isSuccessful })
            assertEquals(3, server.requestCount)
            val requests = List(3) { server.takeRequest(1, TimeUnit.SECONDS)!! }
            assertEquals(listOf("/user/check", "/user/login", "/user/check"), requests.map { it.url.encodedPath })
            assertNull(requests[1].headers["fake-cookie"])
            assertEquals("new", status.fakeCookie.get())
        } finally {
            server.close()
            client.connectionPool.evictAll()
            dispatcher.executorService.shutdown()
        }
    }
}
