package cn.bit101.android.net

import cn.bit101.android.App
import net.gotev.cookiestore.SharedPreferencesCookieStore
import net.gotev.cookiestore.okhttp.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy

/**
 * @author flwfdd
 * @date 2023/3/16 23:14
 * @description _(:з」∠)_
 */
class HttpClient {
    companion object {
        val cookieManager by lazy {
            CookieManager(
//                InMemoryCookieStore("demo"),
                SharedPreferencesCookieStore(App.context, "Cookie"),
                CookiePolicy.ACCEPT_ALL
            )
        }
        val client by lazy {
            OkHttpClient.Builder()
                .cookieJar(
                    JavaNetCookieJar(cookieManager)
                )
                .build()
        }
    }
}