package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.bitlogin.sso.SsoHttpException
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiLogTest {
    @Test
    fun `failure log excludes request credentials`() = runBlocking {
        mockkStatic(Log::class)
        try {
            every { Log.i(any(), any()) } returns 0
            every { Log.e(any(), any()) } returns 0
            val error = SsoHttpException(
                503, "https://sso.bit.edu.cn/cas/login?ticket=secret", "secret",
            )

            assertThrows(SsoHttpException::class.java) {
                runBlocking { logApiCall<Unit>("School", "获取课表") { throw error } }
            }
            verify(exactly = 1) {
                Log.e("School", "获取课表 失败: cn.bit101.bitlogin.sso.SsoHttpException")
            }
        } finally {
            unmockkStatic(Log::class)
        }
    }
}
