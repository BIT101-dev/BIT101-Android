package cn.bit101.android.data.common

import cn.bit101.api.model.common.SmsCodeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsCodeRequestHubTest {

    @Test
    fun `handler publishes pending request and resumes on submit`() = runBlocking {
        val hub = SmsCodeRequestHub()
        val handler = hub.createSmsCodeHandler()

        val result = CompletableDeferred<String>()
        launch {
            result.complete(handler.onSmsCode(SmsCodeRequest("13800000000", "138****0000", "password_second_factor")))
        }

        val pending = waitForPending(hub)
        assertEquals("138****0000", pending.maskedPhone)

        pending.submit("123456")

        assertEquals("123456", result.await())
        assertNull(hub.pending.value)
    }

    @Test
    fun `handler clears pending on cancel`() = runBlocking {
        val hub = SmsCodeRequestHub()
        val handler = hub.createSmsCodeHandler()

        val result = CompletableDeferred<Throwable?>()
        launch {
            result.complete(runCatching { handler.onSmsCode(SmsCodeRequest("13800000000", "138****0000", "password_second_factor")) }.exceptionOrNull())
        }

        val pending = waitForPending(hub)
        pending.cancel()

        val error = result.await()
        assertTrue(error is CancellationException)
        assertNull(hub.pending.value)
    }

    @Test
    fun `submit on already cancelled request is ignored`() = runBlocking {
        val hub = SmsCodeRequestHub()
        val handler = hub.createSmsCodeHandler()

        val result = CompletableDeferred<Throwable?>()
        launch {
            result.complete(runCatching { handler.onSmsCode(SmsCodeRequest("13800000000", "138****0000", "password_second_factor")) }.exceptionOrNull())
        }

        val pending = waitForPending(hub)
        pending.cancel()

        assertTrue(result.await() is CancellationException)

        pending.submit("123456")
        assertNull(hub.pending.value)
    }

    private suspend fun waitForPending(hub: SmsCodeRequestHub): cn.bit101.android.data.repo.PendingSmsCodeRequest {
        val pending = hub.pending.value
        return if (pending != null) pending else {
            kotlinx.coroutines.delay(100)
            waitForPending(hub)
        }
    }
}
