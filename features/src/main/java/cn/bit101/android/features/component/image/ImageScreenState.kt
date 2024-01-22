package cn.bit101.android.features.component.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import cn.bit101.android.features.common.ImageData

/**
 * 图片的变换
 */
data class ImageTransform(
    val scale: Float,
    val offset: Offset,
) {
    companion object {
        val Default = ImageTransform(
            scale = 1.0f,
            offset = Offset(0.0f, 0.0f),
        )
    }
}

/**
 * 图片状态接口
 */
sealed interface BasicImageShowState

/**
 * 图片的状态
 */
class ImageShowState(
    val image: ImageData,
    private val transformState: MutableState<ImageTransform>,
) : BasicImageShowState {
    var transform: ImageTransform
        get() = transformState.value
        set(value) {
            transformState.value = value
        }

    fun resetTransform() {
        transform = ImageTransform.Default
    }
}

/**
 * [ImageShowState] 的 remember
 */
@Composable
fun rememberImageShowState(
    image: ImageData,
) = remember(image) {
    ImageShowState(
        image = image,
        transformState = mutableStateOf(ImageTransform.Default),
    )
}

/**
 * 系列图片的状态
 */
data class SeriesImagesShowState(
    val states: List<ImageShowState>,
    private val indexState: MutableState<Int>,
) : List<ImageShowState>, BasicImageShowState {
    var currentIndex: Int
        get() = indexState.value
        set(value) {
            indexState.value = value
        }

    val currentState: ImageShowState
        get() = states[currentIndex]

    override val size: Int = states.size
    override fun contains(element: ImageShowState) = states.contains(element)
    override fun containsAll(elements: Collection<ImageShowState>) = states.containsAll(elements)
    override fun get(index: Int) = states[index]
    override fun isEmpty() = states.isEmpty()
    override fun iterator() = states.iterator()
    override fun listIterator() = states.listIterator()
    override fun listIterator(index: Int) = states.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int) = states.subList(fromIndex, toIndex)
    override fun lastIndexOf(element: ImageShowState) = states.lastIndexOf(element)
    override fun indexOf(element: ImageShowState) = states.indexOf(element)
}

/**
 * [SeriesImagesShowState] 的 remember
 */
@Composable
fun rememberSeriesImagesShowState(
    images: List<ImageData>,
    initialIndex: Int = 0,
): SeriesImagesShowState = remember(images, initialIndex) {
    SeriesImagesShowState(
        states = images.map {
            ImageShowState(
                image = it,
                transformState = mutableStateOf(ImageTransform.Default),
            )
        },
        indexState = mutableStateOf(initialIndex),
    )
}