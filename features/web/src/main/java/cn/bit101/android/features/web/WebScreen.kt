package cn.bit101.android.features.web

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.theme.LocalThemeIsDark
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberSaveableWebViewState
import com.google.accompanist.web.rememberWebViewNavigator
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import org.json.JSONObject

@Composable
internal fun WebContent(
    mainController: MainController,
    url: String? = null,
) {
    val vm: WebViewModel = hiltViewModel()

    val context = LocalContext.current
    val state = rememberSaveableWebViewState()
    val navigator = rememberWebViewNavigator()

    var progress by remember { mutableFloatStateOf(0f) }

    val scope = rememberCoroutineScope()
    val token by vm.fakeCookie.flow.collectAsState(initial = null)
    val currentToken by rememberUpdatedState(token)
    val sid by vm.sid.flow.collectAsState(initial = null)
    val currentSid by rememberUpdatedState(sid)
    val sessionScript = remember(context) { context.assets.open("bit101-session.js").bufferedReader().use { it.readText() } }
    var refreshSession by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(navigator) {
        val bundle = state.viewState
        if (bundle == null) {
            if(url != null) {
                navigator.loadUrl(url)
            } else {
                navigator.loadUrl(vm.BASE_URL)
            }
        }
    }

    var fileChooserValueCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooseResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            fileChooserValueCallback?.onReceiveValue(arrayOf(Uri.parse(it.data?.dataString)))
        } else {
            fileChooserValueCallback?.onReceiveValue(null)
        }
    }
    val themeIsDark = LocalThemeIsDark.current
    val currentTheme by rememberUpdatedState(themeIsDark)
    fun syncSession(view: WebView, pageLoaded: Boolean) {
        val nativeToken = currentToken ?: return
        val nativeSid = currentSid ?: return
        if (!sameOrigin(view.url, vm.BASE_URL)) return
        val script = "$sessionScript(${JSONObject.quote(vm.BASE_URL)}, ${JSONObject.quote(nativeToken)}, ${JSONObject.quote(nativeSid)}, $currentTheme, $pageLoaded)"
        view.evaluateJavascript(script) { result ->
            // Ignore a callback from a superseded native session or navigation.
            if (nativeToken != currentToken || nativeSid != currentSid || !sameOrigin(view.url, vm.BASE_URL)) return@evaluateJavascript
            refreshSession = result == "\"refresh\""
            if (result == "\"reload\"") view.reload()
        }
    }
    LaunchedEffect(webView, token, sid, themeIsDark) {
        webView?.let { syncSession(it, pageLoaded = false) }
    }

    Column(Modifier.fillMaxSize()) {
        if (refreshSession) {
            TextButton(onClick = { webView?.let { syncSession(it, pageLoaded = true) } }) {
                Text("登录状态已更新，点击刷新页面")
            }
        }
        WebView(
            state = state,
            navigator = navigator,
            modifier = Modifier.weight(1f),
            captureBackPresses = url == null,
            onCreated = {
                webView = it
                it.settings.javaScriptEnabled = true
                it.settings.domStorageEnabled = true //localStorage
            },
            client = object : AccompanistWebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                    syncSession(view, pageLoaded = true)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // 拦截外部链接 使用默认浏览器打开
                    if (!sameOrigin(request?.url?.toString(), vm.BASE_URL)) {
                        val intent = Intent(Intent.ACTION_VIEW, request?.url)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)

                    if (url == "${vm.BASE_URL}/score/" || url == "${vm.BASE_URL}/score") {
                        scope.launch {
                            val sid = vm.sid.get()
                            val password = vm.password.get()
                            if (sid.isNotBlank() && password.isNotBlank() && sameOrigin(view.url, vm.BASE_URL)) {
                                view.evaluateJavascript("""
                                    if (location.origin === ${JSONObject.quote(vm.BASE_URL.trimEnd('/'))}) {
                                        for (const [id, value] of Object.entries({sid: ${JSONObject.quote(sid)}, password: ${JSONObject.quote(password)}})) {
                                            const input = document.getElementById(id);
                                            if (input) { input.value = value; input.dispatchEvent(new Event('input')); }
                                        }
                                    }
                                """.trimIndent(), null)
                            }
                        }
                    }
                }
            },
            chromeClient = object : AccompanistWebChromeClient() {

                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progress = newProgress / 100f
                }

                // 显示文件选择器
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    fileChooserValueCallback = filePathCallback
                    try {
                        fileChooseResultLauncher.launch(fileChooserParams?.createIntent())
                    } catch (e: ActivityNotFoundException) {
                        mainController.snackbar("未找到可以打开的应用！")
                    }
                    return true
                }
            },
        )

        //进度条
        AnimatedVisibility(visible = (progress > 0f && progress < 1f)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun WebScreen(
    mainController: MainController,
    url: String? = null,
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val statusBarHeight = systemBarsPadding.calculateTopPadding()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight),
            color = Color(0xFFFF9A57),
        ) {}
        WebContent(mainController, url)
    }

}

private fun sameOrigin(url: String?, base: String): Boolean {
    if (url == null) return false
    val actual = Uri.parse(url)
    val expected = Uri.parse(base)
    return actual.scheme == expected.scheme && actual.host == expected.host && actual.port == expected.port
}
