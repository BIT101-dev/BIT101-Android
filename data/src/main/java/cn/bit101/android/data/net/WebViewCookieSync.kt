package cn.bit101.android.data.net

import android.webkit.CookieManager as WebCookieManager
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI

/** 将 WebView Cookie 同步到 OkHttp 的 CookieManager。 */
object WebViewCookieSync {

    /** 同步指定地址的 Cookie，返回每个地址写入的数量。 */
    fun sync(target: CookieManager, urls: List<String>): Map<String, Int> {
        val web = WebCookieManager.getInstance()
        val result = mutableMapOf<String, Int>()

        urls.forEach { url ->
            val raw = web.getCookie(url)?.takeIf { it.isNotEmpty() }
            if (raw == null) {
                result[url] = 0
                return@forEach
            }

            val uri = URI.create(url)
            var count = 0
            raw.split(';').forEach { part ->
                val eq = part.indexOf('=')
                if (eq <= 0) return@forEach
                val name = part.substring(0, eq).trim()
                val value = part.substring(eq + 1).trim()
                if (name.isEmpty()) return@forEach
                put(target, uri, name, value)
                count++
            }
            result[url] = count
        }
        return result
    }

    /** 覆盖 CookieStore 中同名的 Cookie。 */
    private fun put(target: CookieManager, uri: URI, name: String, value: String) {
        target.cookieStore.get(uri)
            .filter { it.name == name }
            .forEach { target.cookieStore.remove(uri, it) }

        target.cookieStore.add(
            uri,
            HttpCookie(name, value).apply {
                domain = uri.host
                path = "/"
            },
        )
    }
}
