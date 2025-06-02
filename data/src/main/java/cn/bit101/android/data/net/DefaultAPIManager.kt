package cn.bit101.android.data.net

import cn.bit101.android.data.BuildConfig
import android.util.Log
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.api.Bit101ApiFactory
import cn.bit101.api.helper.Logger
import cn.bit101.api.option.DEFAULT_API_OPTION
import cn.bit101.api.option.DEFAULT_WEB_VPN_URLS
import cn.bit101.api.option.DEV_URLS
import cn.bit101.api.option.PROD_URLS
import kotlinx.coroutines.runBlocking
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Android 的 Logger，用于在 API 模块使用 Android 的 Log
 */
private val androidLogger = object : Logger {
    override fun err(tag: String?, msg: String) { Log.e(tag, msg) }
    override fun warn(tag: String?, msg: String) { Log.w(tag, msg) }
    override fun info(tag: String?, msg: String) { Log.i(tag, msg) }
    override fun debug(tag: String?, msg: String) { Log.d(tag, msg) }
}

internal class DefaultAPIManager @Inject constructor(
    private val loginStatus: LoginStatus
) : APIManager {
    private val cookiesJar = JavaNetCookieJar(loginStatus.cookieManager)

    private val schoolClient = OkHttpClient.Builder()
        .cookieJar(cookiesJar)
        .build()

    private val bit101Client = OkHttpClient.Builder()
        .cookieJar(cookiesJar)
        .addInterceptor {
            val fakeCookie = runBlocking {
                loginStatus.fakeCookie.get()
            }

            val request = it.request().newBuilder()
                .addHeader("fake-cookie", fakeCookie)
                .build()

            val response = it.proceed(request)

            if(response.code == 401) {
                runBlocking {
                    loginStatus.clear()
                }
            }

            response
        }
        .build()

    private var webVpn = false

    private val localUrls = if(BuildConfig.DEBUG) DEV_URLS else PROD_URLS
    private val webVpnUrls = if(BuildConfig.DEBUG) DEV_URLS else DEFAULT_WEB_VPN_URLS

    private var _api = createApi(webVpn)

    private fun createApi(webVpn: Boolean) = Bit101ApiFactory.create(
        DEFAULT_API_OPTION.copy(
            bit101Client = bit101Client,
            schoolClient = schoolClient,
            localUrls = localUrls,
            webVpnUrls = webVpnUrls,
            webVpn = webVpn,
        ),
        logger = androidLogger,
    )

    override fun switch(webVpn: Boolean) {
        if(webVpn != this.webVpn) {
            this.webVpn = webVpn
            _api = createApi(webVpn)
        }
    }

    override val api = _api
}