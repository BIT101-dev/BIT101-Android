package cn.bit101.android.net

import android.util.Log
import cn.bit101.android.App
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.datastore.UserDataStore
import cn.bit101.api.Bit101Api
import cn.bit101.api.Bit101ApiFactory
import cn.bit101.api.option.DEFAULT_API_OPTION
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.gotev.cookiestore.SharedPreferencesCookieStore
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy


object APIManager {
    private val bit101Client = OkHttpClient.Builder()
        .addInterceptor {
            // 自动添加fake-cookie
            val fakeCookie = runBlocking {
                UserDataStore.fakeCookie.get()
            }

            val request = it.request().newBuilder()
                .addHeader("fake-cookie", fakeCookie)
                .build()

            Log.i("fakeCookie", fakeCookie)

            Log.i("APIManager", request.url.toString())

            it.proceed(request)


//            Log.i("APIManager", "request: ${request.url}")
//            Log.i("APIManager", "headers: ${request.headers.toMultimap()}")
//            Log.i("APIManager", "query: ${request.url.encodedQuery}")
//
//
//            val bufferedSink = okio.Buffer()
//            request.body?.writeTo(bufferedSink)
//            val body = bufferedSink.toString()
//            val contentType = request.body?.contentType()
//
//            Log.i("APIManager", "body: $body")
//
//            if(request.method == "GET") {
//                return@addInterceptor it.proceed(request)
//            }
//            val newRequest = request.newBuilder()
//                .method(request.method, body.toRequestBody(contentType))
//                .build()
//
//            val response = it.proceed(newRequest)
//
//            if(response.code == 401) {
//                runBlocking {
//                    UserDataStore.clear()
//                }
//            }
//            response
        }
        .build()

    private val cookieManager = CookieManager(
        SharedPreferencesCookieStore(App.context, "Cookie"),
        CookiePolicy.ACCEPT_ALL
    )

    private val schoolClient = OkHttpClient.Builder()
        .cookieJar(
            JavaNetCookieJar(
                cookieManager
            )
        )
//        .addInterceptor {
//            val request = it.request().newBuilder()
//                .build()
//            Log.i("APIManager", request.url.toString())
//            Log.i("APIManager", request.headers.toMultimap().toString())
//
//            val response = it.proceed(request)
//
//            Log.i("APIManager", response.headers.toMultimap().toString())
//
//            response
//        }
        .addNetworkInterceptor {
            val request = it.request().newBuilder()
                .build()
            Log.i("APIManager", request.url.toString())
            Log.i("APIManager", request.headers.toMultimap().toString())

            val response = it.proceed(request)

            Log.i("APIManager", response.headers.toMultimap().toString())

            response
        }
        .build()

    init {
        // 获取
        MainScope().launch {
            SettingDataStore.settingUseWebVpn.flow.collect {
                switch(it)
            }
        }
    }


    fun clearCookie() {
        cookieManager.cookieStore.removeAll()
    }

    private var webVpn = false

    private var api = Bit101ApiFactory.create(
        DEFAULT_API_OPTION.copy(
            bit101Client = bit101Client,
            schoolClient = schoolClient,
            webVpn = webVpn,
        )
    )
    fun switch(webVpn: Boolean) {
        if(webVpn != this.webVpn) {
            this.webVpn = webVpn
            api = Bit101ApiFactory.create(
                DEFAULT_API_OPTION.copy(
                    bit101Client = bit101Client,
                    schoolClient = schoolClient,
                    webVpn = this.webVpn,
                )
            )
        }
    }

    fun switchToWebVpn() = switch(true)
    fun switchToLocal() = switch(false)

    val API: Bit101Api
        get() = api
}

val BIT101API: Bit101Api
    get() = APIManager.API