package cn.bit101.android.data.repo.base

import cn.bit101.bitlogin.sso.SsoHttpException
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class LoginExceptionTest {
    @Test
    fun `HTTP failures preserve step status and retry after without exposing body`() {
        val raw = okhttp3.Response.Builder().request(Request.Builder().url("https://example.test/").build())
            .protocol(Protocol.HTTP_1_1).code(429).message("limited").header("Retry-After", "120").build()
        val response = Response.error<String>("sensitive upstream body".toResponseBody(), raw)
        val failure = assertThrows(LoginException::class.java) { response.loginBody("verify") }
        assertEquals(LoginRefreshResult.TRANSIENT, failure.result)
        assertEquals("verify", failure.step)
        assertEquals(429, failure.statusCode)
        assertEquals("120", failure.retryAfter)
        assertFalse(failure.message!!.contains("sensitive"))
    }

    @Test
    fun `HTTP forbidden is not evidence of wrong password`() {
        assertEquals(LoginRefreshResult.ERROR, SsoHttpException(403, "test", "blocked").asLoginException("school").result)
    }

    @Test
    fun `empty success is a protocol error`() {
        val failure = assertThrows(LoginException::class.java) { Response.success<String>(null).loginBody("register") }
        assertEquals(LoginRefreshResult.ERROR, failure.result)
        assertEquals("register", failure.step)
    }
}
