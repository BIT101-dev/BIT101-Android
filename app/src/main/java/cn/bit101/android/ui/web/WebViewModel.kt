package cn.bit101.android.ui.web

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import cn.bit101.android.App
import cn.bit101.android.datastore.UserDataStore
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class WebViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    val BASE_URL = "https://bit101.cn"

    val progressLiveData = MutableLiveData(0f)

    val webViewClient = object : AccompanistWebViewClient() {
        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            // 注入fake-cookie
            viewModelScope.launch {
                val fakeCookie = UserDataStore.fakeCookie.get()
                if (fakeCookie.isNotEmpty()) {
                    view.evaluateJavascript(
                        """
                            if (!window.localStorage.getItem("store")) {
                                window.localStorage.setItem("store", '{"fake_cookie":"$fakeCookie"}')
                            } else {
                                store_tmp = JSON.parse(window.localStorage.store);
                                store_tmp.fake_cookie = "$fakeCookie";
                                window.localStorage.setItem("store", JSON.stringify(store_tmp));
                            }
                        """.trimIndent(), null
                    )
                }
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            // 拦截外部链接 使用默认浏览器打开
            if (request?.url?.toString()?.startsWith(BASE_URL) != true) {
                val intent = Intent(Intent.ACTION_VIEW, request?.url)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)

            val sid = runBlocking {
                UserDataStore.loginSid.get()
            }

            val password = runBlocking {
                UserDataStore.loginSid.get()
            }

            // 自动填充成绩查询学号密码
            if ((url == "$BASE_URL/#/score/" || url == "$BASE_URL/#/score") && sid.isNotEmpty() && password.isNotEmpty()) {
                val script = """
                    document.getElementById("sid").value = "$sid";
                    document.getElementById("sid").dispatchEvent(new Event('input'));
                    document.getElementById("password").value = "$password";
                    document.getElementById("password").dispatchEvent(new Event('input'));
                """.trimIndent()
                view.evaluateJavascript(script, null)

            }
        }
    }

    val chromeClient = object : AccompanistWebChromeClient() {
        init {
            MainScope().launch {
                App.activityResult.collect {
                    uploadMessage?.onReceiveValue(it.toTypedArray())
                }
            }

        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressLiveData.value = newProgress / 100f
        }

        // 用于传递上传文件
        var uploadMessage: ValueCallback<Array<Uri>>? = null

        // 显示文件选择器
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            uploadMessage = filePathCallback
            App.activityResultLauncher.launch("*/*")
            return true
        }
    }
}