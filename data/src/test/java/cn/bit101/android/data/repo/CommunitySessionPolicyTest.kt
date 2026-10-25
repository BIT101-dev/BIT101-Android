package cn.bit101.android.data.repo

import org.junit.Assert.*
import org.junit.Test
import java.util.Base64

class CommunitySessionPolicyTest {
    private fun token(exp: Long) = "header." + Base64.getUrlEncoder().withoutPadding()
        .encodeToString("{\"exp\":$exp}".toByteArray()) + ".signature"

    @Test fun `known expiry renews once within one minute while opaque old tokens remain usable`() {
        assertFalse(CommunitySessionPolicy.usable("", 100_000))
        assertTrue(CommunitySessionPolicy.usable("legacy-token", 100_000))
        assertTrue(CommunitySessionPolicy.usable(token(161), 100_000))
        assertFalse(CommunitySessionPolicy.usable(token(160), 100_000))
        assertFalse(CommunitySessionPolicy.usable(token(99), 100_000))
    }

    @Test fun `retry after date never shortens local backoff`() {
        assertEquals(1_800_000L, CommunitySessionPolicy.retryAt(0, 1, "invalid"))
        assertEquals(1_800_000L, CommunitySessionPolicy.retryAt(0, 1, "-1"))
        assertEquals(3_600_000L, CommunitySessionPolicy.retryAt(0, 1, "Thu, 1 Jan 1970 01:00:00 GMT"))
    }
    @Test fun `identity exponential cap is bounded and longer retry after takes precedence`() {
        val day = CommunitySessionPolicy.IDENTITY_RETRY_INTERVAL
        assertEquals(day, CommunitySessionPolicy.retryAt(0, 0, null, identity = true))
        assertEquals(8 * day, CommunitySessionPolicy.retryAt(0, Int.MAX_VALUE, null, identity = true))
        assertEquals(3 * day, CommunitySessionPolicy.retryAt(0, 2, "259200", identity = true))
        assertEquals(8 * day, CommunitySessionPolicy.retryAt(0, 4, "60", identity = true))
        assertEquals(10 * day, CommunitySessionPolicy.retryAt(0, 4, "864000", identity = true))
        assertEquals(day, CommunitySessionPolicy.retryAt(0, 1, Long.MAX_VALUE.toString(), identity = true))
    }

}
