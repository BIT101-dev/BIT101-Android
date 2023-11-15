package cn.bit101.android.ui.gallery.common

import android.net.Uri
import cn.bit101.api.model.common.Image

sealed interface UploadImageState {
    object Loading : UploadImageState
    object Fail : UploadImageState
    data class Success(val image: Image) : UploadImageState
}


sealed interface ImageData {
    data class Local(val uri: Uri) : ImageData
    data class Remote(val image: Image) : ImageData
}


data class UploadImageData(
    val ifUpdate: Boolean,
    val images: List<Pair<ImageData, UploadImageState>>
)