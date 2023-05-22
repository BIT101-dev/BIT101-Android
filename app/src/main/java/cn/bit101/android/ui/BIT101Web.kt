package cn.bit101.android.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.App
import cn.bit101.android.database.DataStore
import cn.bit101.android.viewmodel.BIT101WebViewModel
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberSaveableWebViewState
import com.google.accompanist.web.rememberWebViewNavigator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * @author flwfdd
 * @date 14/05/2023 00:15
 * @description _(:з」∠)_
 */

@Composable
fun BIT101Web(vm: BIT101WebViewModel = viewModel()) {
    val context = LocalContext.current
    val state = rememberSaveableWebViewState()
    val navigator = rememberWebViewNavigator()

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(navigator) {
        val bundle = state.viewState
        if (bundle == null) {
            navigator.loadUrl(vm.BASE_URL)
        }
    }

    val scope = rememberCoroutineScope()

    WebView(
        state = state,
        navigator = navigator,
        modifier = Modifier.fillMaxSize(),
        onCreated = {
            it.settings.javaScriptEnabled = true
            it.settings.domStorageEnabled = true //localStorage
        },
        client = object : AccompanistWebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 注入fake-cookie
                scope.launch {
                    DataStore.fakeCookieFlow.collect {
                        if (it != null)
                            view.evaluateJavascript(
                                """
                            if (!window.localStorage.getItem("store")) {
                                window.localStorage.setItem("store", '{"fake_cookie":"$it"}')
                            } else {
                                store_tmp = JSON.parse(window.localStorage.store);
                                store_tmp.fake_cookie = "$it";
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
                if (request?.url?.toString()?.startsWith(vm.BASE_URL) != true) {
                    val intent = Intent(Intent.ACTION_VIEW, request?.url)
                    context.startActivity(intent)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)

                // 自动填充成绩查询学号密码
                if ((url == vm.BASE_URL + "/#/score/" || url == vm.BASE_URL + "/#/score") && vm.sid != null && vm.password != null) {
                    val script = """
                    document.getElementById("sid").value = "${vm.sid}";
                    document.getElementById("sid").dispatchEvent(new Event('input'));
                    document.getElementById("password").value = "${vm.password}";
                    document.getElementById("password").dispatchEvent(new Event('input'));
                """.trimIndent()
                    view.evaluateJavascript(script, null)

                }
            }
        },
        chromeClient = object : AccompanistWebChromeClient() {
            init {
                MainScope().launch {
                    App.activityResult.collect {
                        uploadMessage?.onReceiveValue(it.toTypedArray())
                    }
                }

            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progress = newProgress / 100f
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
    )

    //进度条
    AnimatedVisibility(visible = (progress > 0f && progress < 1f)) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BIT101WebPreview() {
    BIT101Web()
}