package cn.bit101.android.ui.gallery.common

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
 * 这里的图片数据有两种，一种是本地图片，一种是远程图片
 * - 本地图片用Uri表示，一般是用户通过选择Activity选择的图片
 * - 远程图片用Image表示，是从服务器获取的图片
 */
sealed interface ImageData {
    data class Local(val uri: Uri) : ImageData
    data class Remote(val image: Image) : ImageData
}


/**
 * 上传图片的数据
 * @param ifUpdate 是否上传图片
 * @param images 图片数据，是一个Pair列表，Pair的第一个数据表示图片数据，第二个数据表示图片的上传状态
 */
data class UploadImageData(
    val ifUpdate: Boolean,
    val images: List<Pair<ImageData, UploadImageState>>
)