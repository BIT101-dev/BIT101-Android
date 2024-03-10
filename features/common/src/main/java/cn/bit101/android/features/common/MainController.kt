package cn.bit101.android.features.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import cn.bit101.android.features.common.component.image.ImageHostState
import cn.bit101.android.features.common.component.snackbar.SnackbarState
import cn.bit101.android.features.common.helper.ImageData
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.common.nav.NavDestConfig
import cn.bit101.android.features.common.nav.navigate
import cn.bit101.api.model.common.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainController(
    val scope: CoroutineScope,
    private val navController: NavHostController,
    val snackbarHostState: SnackbarState,
    val imageHostState: ImageHostState,
) {
    fun navigate(dest: NavDest) =
        navController.navigate(dest)

    fun navigate(dest: NavDest, builder: NavOptionsBuilder.() -> Unit) =
        navController.navigate(dest, navOptions(builder))

    @Composable
    fun currentDestConfigAsState(): State<NavDestConfig?> {
        return navController.currentBackStackEntryFlow.map {
            NavDestConfig.fromRoute(it.destination.route)
        }.collectAsState(initial = null)
    }

    val startDestId: Int
        get() = navController.graph.startDestinationId

    fun popBackStack() = navController.popBackStack()

    fun snackbar(message: String) {
        scope.launch {
            snackbarHostState.show(message)
        }
    }

    fun copyText(cm: ClipboardManager, text: AnnotatedString) {
        Log.i("copyText", text.text)
        cm.setText(text)
        snackbar(message = "已复制")
    }

    fun copyText(cm: ClipboardManager, text: String?) {
        if (text == null) return
        copyText(cm, AnnotatedString(text))
    }

    fun openUrl(url: String, ctx: Context) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        ctx.startActivity(intent)
    }

    fun openWebPage(url: String) {
        navigate(NavDest.Web(url))
    }

    fun openPoster(id: Long, ctx: Context) {
        openUrl("https://bit101.cn/gallery/$id", ctx)
    }

    fun showImage(image: Image) = imageHostState.showSingleImage(ImageData.Remote(image))

    fun showImages(
        index: Int = 0,
        images: List<Image>,
    ) = imageHostState.showSeriesImages(images.map { ImageData.Remote(it) }, index)
}