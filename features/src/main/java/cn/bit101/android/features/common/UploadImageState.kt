package cn.bit101.android.features.common

import android.net.Uri
import cn.bit101.api.model.common.Image


/**
 * 上传图片的状态
 */
sealed interface UploadImageState {
    object Loading : UploadImageState
    object Fail : UploadImageState
    data class Success(val image: Image) : UploadImageState
}

/**
 * 图片数据
 * 这里的图片数据有三种，一种是本地图片，一种是远程图片（从bit101上得到的），一种是远程图片只有一个Url
 * - 本地图片用Uri表示，一般是用户通过选择Activity选择的图片
 * - BIT101上的远程图片用Image表示，是从服务器获取的图片
 * - 远程图片只有一个Url，一般是用户通过输入Url的方式添加的图片
 */
sealed interface ImageData {
    data class Local(val uri: Uri) : ImageData
    data class Remote(val image: Image) : ImageData
    data class RemoteUseUrl(val url: String) : ImageData
}


fun ImageData.lowModel(): Any = when(this) {
    is ImageData.Local -> uri
    is ImageData.Remote -> Uri.parse(image.lowUrl)
    is ImageData.RemoteUseUrl -> Uri.parse(url)
}

fun ImageData.highModel(): Any = when(this) {
    is ImageData.Local -> uri
    is ImageData.Remote -> Uri.parse(image.url)
    is ImageData.RemoteUseUrl -> Uri.parse(url)
}

/**
 * 带上传状态的图片数据
 * @param imageData 图片数据，[ImageData]
 * @param uploadImageState 上传图片的状态，[UploadImageState]
 */
data class ImageDataWithUploadState(
    val imageData: ImageData,
    val uploadImageState: UploadImageState
)

fun ImageDataWithUploadState.lowModel(): Any = when(uploadImageState) {
    is UploadImageState.Success -> uploadImageState.image.lowUrl
    else -> imageData.lowModel()
}

fun ImageDataWithUploadState.highModel(): Any = when(uploadImageState) {
    is UploadImageState.Success -> uploadImageState.image.url
    else -> imageData.highModel()
}

/**
 * 上传图片的数据
 * @param ifUpload 是否上传图片
 * @param images 带上传状态的图片数据，[ImageDataWithUploadState]
 */
data class UploadImageData(
    val ifUpload: Boolean,
    val images: List<ImageDataWithUploadState>
) {
    companion object {
        val default = UploadImageData(
            ifUpload = false,
            images = emptyList()
        )
    }
}