package cn.bit101.android.data.net

import cn.bit101.android.data.BuildConfig
import android.util.Log
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.LoginRefreshResult
import cn.bit101.android.data.repo.base.LoginRepo
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
import javax.inject.Provider

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
    private val loginStatus: LoginStatus,
    private val loginRepoProvider: Provider<LoginRepo>,
) : APIManager {
    private val cookiesJar = JavaNetCookieJar(loginStatus.cookieManager)

    private val schoolClient = OkHttpClient.Builder()
        .cookieJar(cookiesJar)
        .build()

    private val bit101Client = OkHttpClient.Builder()
        .cookieJar(cookiesJar)
        .addInterceptor { chain ->
            val fakeCookie = runBlocking {
                loginStatus.fakeCookie.get()
            }

            val request = chain.request().newBuilder()
                .addHeader("fake-cookie", fakeCookie)
                .build()

            val response = chain.proceed(request)

            if (response.code == 401) {
                // 已重试过，不再重试
                if (chain.request().header(AUTH_RETRIED_HEADER) != null) {
                    runBlocking {
                        loginStatus.clear()
                    }
                } else {
                    // 静默刷新整个会话（不弹短信验证码），成功后携带新 fake-cookie 重试一次。
                    // 使用非阻塞 tryRefreshLogin：互斥锁被持锁流程（checkLogin/login/refreshLogin）占用时
                    // 立即返回 BUSY，避免在该流程内部重入同一把非重入锁而死锁
                    val refreshed = runBlocking {
                        loginRepoProvider.get().tryRefreshLogin()
                    }
                    when (refreshed) {
                        LoginRefreshResult.SUCCESS -> {
                            val newFakeCookie = runBlocking {
                                loginStatus.fakeCookie.get()
                            }
                            val retryRequest = chain.request().newBuilder()
                                .addHeader("fake-cookie", newFakeCookie)
                                .addHeader(AUTH_RETRIED_HEADER, "1")
                                .build()
                            return@addInterceptor chain.proceed(retryRequest)
                        }

                        // 持锁流程自身负责刷新与登录态决策，这里直接返回 401，绝不清除登录态
                        LoginRefreshResult.BUSY ->
                            return@addInterceptor response

                        // 其他（NEEDS_INTERACTIVE / TRANSIENT / FAILED）维持原清理逻辑
                        else -> runBlocking {
                            loginStatus.clear()
                        }
                    }
                }
            }

            response
        }
        .build()

    private var webVpn = runBlocking { loginStatus.webVpn.get() }

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

    override suspend fun switch(webVpn: Boolean) {
        if(webVpn != this.webVpn) {
            this.webVpn = webVpn
            _api = createApi(webVpn)
        }
        loginStatus.webVpn.set(webVpn)
    }

    override val api
        get() = _api

    private companion object {
        const val AUTH_RETRIED_HEADER = "X-BIT101-AUTH-RETRIED"
    }
}
