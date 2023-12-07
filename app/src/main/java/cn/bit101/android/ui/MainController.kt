package cn.bit101.android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation.NavHostController
import cn.bit101.android.ui.common.ImageData
import cn.bit101.android.ui.component.image.ImageHostState
import cn.bit101.android.ui.component.snackbar.SnackbarState
import cn.bit101.api.model.common.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainController(
    val scope: CoroutineScope,
    val navController: NavHostController,
    val snackbarHostState: SnackbarState,
    val imageHostState: ImageHostState,
) {

    fun navigate(route: String) {
        navController.navigate(route)
    }

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

    fun copyText(cm: ClipboardManager, text: String) {
        copyText(cm, AnnotatedString(text))
    }

    fun openUrl(ctx: Context, url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        ctx.startActivity(intent)
    }
    fun showImage(image: Image) = imageHostState.showSingleImage(ImageData.Remote(image))

    fun showImages(
        index: Int = 0,
        images: List<Image>,
    ) = imageHostState.showSeriesImages(images.map { ImageData.Remote(it) }, index)
}