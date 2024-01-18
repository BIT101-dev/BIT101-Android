package cn.bit101.android.net

import cn.bit101.android.BuildConfig
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.net.base.APIManager
import cn.bit101.android.net.basic.CookiesJar
import cn.bit101.api.Bit101ApiFactory
import cn.bit101.api.option.DEFAULT_API_OPTION
import cn.bit101.api.option.DEFAULT_WEB_VPN_URLS
import cn.bit101.api.option.DEV_URLS
import cn.bit101.api.option.PROD_URLS
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.net.CookieManager
import javax.inject.Inject

class DefaultAPIManager @Inject constructor(
    private val loginStatusManager: LoginStatusManager
) : APIManager {
    private val cookiesJar = CookiesJar(loginStatusManager.cookieManager)

    private var schoolClient = OkHttpClient.Builder()
        .cookieJar(cookiesJar)
        .build()

    private var bit101Client = OkHttpClient.Builder()
        .cookieJar(cookiesJar)
        .addInterceptor {
            val fakeCookie = runBlocking {
                loginStatusManager.fakeCookie.get()
            }

            val request = it.request().newBuilder()
                .addHeader("fake-cookie", fakeCookie)
                .build()

            val response = it.proceed(request)

            if(response.code == 401) {
                runBlocking {
                    loginStatusManager.clear()
                }
            }

            response
        }
        .build()

    private var webVpn = false

    private val localUrls = if(BuildConfig.DEBUG) DEV_URLS else PROD_URLS
    private val webVpnUrls = if(BuildConfig.DEBUG) DEV_URLS else DEFAULT_WEB_VPN_URLS

    private var _api = Bit101ApiFactory.create(
        DEFAULT_API_OPTION.copy(
            bit101Client = bit101Client,
            schoolClient = schoolClient,
            localUrls = localUrls,
            webVpnUrls = webVpnUrls,
            webVpn = webVpn,
        )
    )

    fun switch(webVpn: Boolean) {
        if(webVpn != this.webVpn) {
            this.webVpn = webVpn
            _api = Bit101ApiFactory.create(
                DEFAULT_API_OPTION.copy(
                    bit101Client = bit101Client,
                    schoolClient = schoolClient,
                    localUrls = localUrls,
                    webVpnUrls = webVpnUrls,
                    webVpn = this.webVpn,
                )
            )
        }
    }

    fun switchToWebVpn() = switch(true)
    fun switchToLocal() = switch(false)

    override val api = _api
}