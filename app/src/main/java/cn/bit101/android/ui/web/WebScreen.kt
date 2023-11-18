package cn.bit101.android.ui.web

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberSaveableWebViewState
import com.google.accompanist.web.rememberWebViewNavigator

@Composable
fun WebScreen(
    vm: WebViewModel = hiltViewModel()
) {
    val state = rememberSaveableWebViewState()
    val navigator = rememberWebViewNavigator()

    val progress by vm.progressLiveData.observeAsState()

    LaunchedEffect(navigator) {
        val bundle = state.viewState
        if (bundle == null) {
            navigator.loadUrl(vm.BASE_URL)
        }
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = Modifier.fillMaxSize(),
        onCreated = {
            it.settings.javaScriptEnabled = true
            it.settings.domStorageEnabled = true //localStorage
        },
        client = vm.webViewClient,
        chromeClient = vm.chromeClient,
    )

    //进度条
    AnimatedVisibility(visible = (progress != null && progress!! > 0f && progress!! < 1f)) {
        LinearProgressIndicator(
            progress = { progress ?: 0.0f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round,
        )
    }
}