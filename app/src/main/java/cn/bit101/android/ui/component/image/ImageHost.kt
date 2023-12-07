package cn.bit101.android.ui.component.image

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cn.bit101.android.ui.common.ImageData

/**
 * 用于 [ImageHost] ，表示展示的图片数据
 */
sealed interface ImageHostData {
    data class Single(
        val image: ImageData,
    ) : ImageHostData

    data class Series(
        val images: List<ImageData>,
        val index: Int,
    ) : ImageHostData

    object None: ImageHostData
}

/**
 * [ImageHost] 的状态
 */
class ImageHostState(
    private val dataState: MutableState<ImageHostData>,
) {
    val data: ImageHostData
        get() = dataState.value

    private fun show(data: ImageHostData) {
        dataState.value = data
    }

    fun showSingleImage(image: ImageData) {
        show(ImageHostData.Single(image))
    }

    fun showSeriesImages(images: List<ImageData>, index: Int = 0) {
        show(ImageHostData.Series(images, index))
    }

    fun dismiss() {
        show(ImageHostData.None)
    }
}

/**
 * 类似 SnackbarHost ，用于展示图片
 */
@Composable
fun ImageHost(
    modifier: Modifier = Modifier,
    state: ImageHostState,
    onOpenUrl: (String) -> Unit,
) {
    Box(modifier = modifier) {
        when(val data = state.data) {
            is ImageHostData.Single -> {
                ImageScreen(
                    image = data.image,
                    onOpenUrl = onOpenUrl,
                    onDismiss = state::dismiss
                )
            }
            is ImageHostData.Series -> {
                ImageScreen(
                    images = data.images,
                    initialIndex = data.index,
                    onOpenUrl = onOpenUrl,
                    onDismiss = state::dismiss
                )
            }
            else -> {}
        }
    }
}

/**
 * 对 [ImageHostState] 的 remember
 */
@Composable
fun rememberImageHostState() = remember {
    ImageHostState(
        dataState = mutableStateOf(ImageHostData.None)
    )
}