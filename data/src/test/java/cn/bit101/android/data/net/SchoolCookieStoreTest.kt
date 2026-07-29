package cn.bit101.android.data.net

import cn.bit101.api.model.common.SchoolCookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI

class SchoolCookieStoreTest {
    private val now = 1_000L

    @Test
    fun `converts all cookie attributes`() {
        val cookie = SchoolCookie(
            name = "SESSION",
            value = "secret",
            domain = ".bit.edu.cn",
            path = "/cas",
            secure = true,
            expiresEpochSeconds = now + 600,
        )

        val converted = with(SchoolCookieStore) { cookie.toHttpCookie(now) }

        requireNotNull(converted)
        assertEquals("SESSION", converted.name)
        assertEquals("secret", converted.value)
        assertEquals(".bit.edu.cn", converted.domain)
        assertEquals("/cas", converted.path)
        assertTrue(converted.secure)
        assertEquals(600, converted.maxAge)
    }

    @Test
    fun `keeps session cookies and normalizes blank paths`() {
        val cookie = schoolCookie(path = "", expiresEpochSeconds = null)

        val converted = with(SchoolCookieStore) { cookie.toHttpCookie(now) }

        requireNotNull(converted)
        assertEquals("/", converted.path)
        assertEquals(-1, converted.maxAge)
    }

    @Test
    fun `ignores expired cookies`() {
        val cookie = schoolCookie(expiresEpochSeconds = now)

        val converted = with(SchoolCookieStore) { cookie.toHttpCookie(now) }

        assertEquals(null, converted)
    }

    @Test
    fun `rejects cookies without domains`() {
        val cookie = schoolCookie(domain = "")

        assertThrows(IllegalArgumentException::class.java) {
            with(SchoolCookieStore) { cookie.toHttpCookie(now) }
        }
    }

    @Test
    fun `replaces school cookies without clearing backend cookies`() {
        val store = CookieManager().cookieStore
        store.add(
            URI("https://sso.bit.edu.cn/"),
            HttpCookie("OLD", "old").apply {
                domain = "sso.bit.edu.cn"
                path = "/"
            },
        )
        store.add(
            URI("https://bit101.cn/"),
            HttpCookie("BACKEND", "keep").apply {
                domain = "bit101.cn"
                path = "/"
            },
        )

        SchoolCookieStore.replace(store, listOf(schoolCookie()), now)

        assertFalse(store.cookies.any { it.name == "OLD" })
        assertTrue(store.cookies.any { it.name == "BACKEND" && it.value == "keep" })
        assertTrue(store.cookies.any { it.name == "SESSION" && it.value == "new" })
    }

    @Test
    fun `keeps same-name cookies with different paths`() {
        val store = CookieManager().cookieStore
        SchoolCookieStore.replace(
            store,
            listOf(
                schoolCookie(path = "/cas"),
                schoolCookie(path = "/jwapp"),
            ),
            now,
        )

        assertEquals(2, store.cookies.count { it.name == "SESSION" })
    }

    private fun schoolCookie(
        domain: String = "sso.bit.edu.cn",
        path: String = "/",
        expiresEpochSeconds: Long? = now + 600,
    ) = SchoolCookie(
        name = "SESSION",
        value = "new",
        domain = domain,
        path = path,
        secure = true,
        expiresEpochSeconds = expiresEpochSeconds,
    )
}
