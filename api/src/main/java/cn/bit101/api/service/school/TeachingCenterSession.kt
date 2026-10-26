package cn.bit101.api.service.school

import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.Config
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.http.HttpResponse
import cn.bit101.bitlogin.sso.BitSsoClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

internal fun teachingCenterBase(webVpn: Boolean): String =
    (if (webVpn) Config.Urls.webvpn else Config.Urls.campus).getValue("jxzxehall_app")

internal fun HttpClient.schoolCookies(): List<SchoolCookie> = cookieDetails().map {
    SchoolCookie(it.name, it.value, it.domain, it.path, it.secure, it.expires)
}

internal suspend fun HttpClient.restoreSchoolCookies(cookies: List<SchoolCookie>) {
    val now = Instant.now().epochSecond
    cookies.filter { it.expiresEpochSeconds == null || it.expiresEpochSeconds > now }.forEach {
        addCookie(it.name, it.value, it.domain, it.path, it.secure, it.expiresEpochSeconds)
    }
}

/** 复用当前凭据对应的业务 Cookie 和初始化状态；每次请求结束都关闭传输客户端。 */
internal class TeachingCenterSession(private val createClient: () -> HttpClient = { HttpClient() }) {
    private val mutex = Mutex()
    private var source: List<SchoolCookie> = emptyList()
    private var route: Boolean? = null
    private var cookies: List<SchoolCookie> = emptyList()
    private val prepared = mutableSetOf<String>()

    suspend fun <T> use(input: List<SchoolCookie>, webVpn: Boolean, app: String = "wdkbby",
                        block: suspend (TeachingCenterRequests) -> T): T = mutex.withLock {
        if (input.isEmpty()) throw SchoolSessionExpired()
        // java.net.CookieStore 转出的绝对过期时间可能随读取变化，身份以 Cookie 内容和作用域比较。
        val identity = input.map { it.copy(expiresEpochSeconds = null) }
        if (source != identity || route != webVpn) {
            source = identity
            route = webVpn
            cookies = input
            prepared.clear()
        }
        val client = createClient()
        try {
            client.headers.putAll(BitSsoClient.BROWSER_DEFAULT_HEADERS)
            client.restoreSchoolCookies(cookies)
            val requests = TeachingCenterRequests(client, teachingCenterBase(webVpn))
            // 校区、楼宇和空教室也依赖教学中心公共初始化，不能只初始化目标应用。
            if ("wdkbby" !in prepared) {
                requests.prepare("wdkbby")
                prepared += "wdkbby"
            }
            if (app !in prepared) {
                requests.prepare(app)
                prepared += app
            }
            val result = block(requests)
            cookies = client.schoolCookies()
            result
        } catch (e: Exception) {
            prepared.clear()
            cookies = input
            if (e.isSchoolConnectionFailure()) throw SchoolRouteUnavailable(webVpn, e)
            throw e
        } finally {
            client.close()
        }
    }
}

internal class TeachingCenterRequests(private val client: HttpClient, private val base: String) {
    suspend fun prepare(app: String) {
        get("/jwapp/sys/$app/*default/index.do").validateTeachingCenter()
        if (app == "wdkbby") {
            get("/jwapp/sys/funauthapp/api/getAppConfig/wdkbby-5959167891382285.do").validateTeachingCenter()
            get("/jwapp/i18n.do?appName=wdkbby&EMAP_LANG=zh").validateTeachingCenter()
        }
    }

    suspend fun get(path: String): HttpResponse = client.get(base + path, headers = Config.Headers.jxzxehall)

    suspend fun json(path: String, form: Map<String, String>? = null) =
        (if (form == null) client.get(base + path, headers = Config.Headers.base)
        else client.post(base + path, headers = Config.Headers.base, data = form))
            .teachingCenterJson()
}
