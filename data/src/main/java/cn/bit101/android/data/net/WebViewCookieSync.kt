package cn.bit101.android.data.net

import android.webkit.CookieManager as WebCookieManager
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI

/**
 * 把 WebView 里的 cookie 同步到 OkHttp 使用的 `java.net.CookieManager`。
 *
 * ## 为什么需要它
 *
 * 课程中心（eclass）的登录在 **WebView** 里完成（用户手走统一身份认证），
 * 那一刻 cookie 只存在于 WebView 的存储中；而后续接口请求由 **OkHttp** 发出。
 * 两者的 cookie 存储**彼此独立** —— 不同步的话，登录看起来是成功的，
 * 但接口全部拿不到数据（302 回登录页），而且很难看出是「没登录」还是「同步漏了」。
 *
 * ## 为什么不直接复用座位模块那份
 *
 * `features:seat` 的 `SeatHttp.syncWebViewCookies()` 做的是同一件事，但它是
 * `internal` 且依赖座位模块自己的 cookie 桥，跨模块取不到。把公共部分抽出来意味着
 * 要改动已经在稳定运行的座位模块 —— 收益不抵风险，所以这里单独实现一份。
 * （两份实现都很短，且注释里互相指认。）
 *
 * ⚠️ 目标 `cookieManager` 是**全局共用**的那一份（`LoginStatus.cookieManager`），
 * 学校域下的会话就是这样在 BIT101 登录 / 座位 / eclass 之间互通的。
 */
object WebViewCookieSync {

    /**
     * 把 [urls] 这些地址在 WebView 中的 cookie 写入 [target]。
     *
     * @return 每个地址同步成功的 cookie 数量（仅用于日志排障）
     */
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

    /** 以「同名覆盖」写入，避免同名 cookie 在 store 里越堆越多、旧值先生效。 */
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
