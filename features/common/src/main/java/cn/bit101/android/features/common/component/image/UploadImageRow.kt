package cn.bit101.android.features.common.component.image

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.helper.ImageDataWithUploadState
import cn.bit101.android.features.common.helper.UploadImageState
import cn.bit101.android.features.common.helper.lowModel
import cn.bit101.api.model.common.Image
import coil.compose.AsyncImage

/**
 * 上传图片的部分
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UploadImageRow(

    /**
     * 图片列表
     */
    images: List<ImageDataWithUploadState>,

    /**
     * 打开图片
     */
    onOpenImage: (Image) -> Unit,

    /**
     * 打开删除图片的对话框
     */
    onOpenDeleteDialog: (Int) -> Unit,

    /**
     * 删除上传失败的图片
     */
    onDeleteFailImage: (Int) -> Unit,
) {
    val view = LocalView.current
    LazyRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(images) { index, image ->
            Box(modifier = Modifier.padding(end = 4.dp).size(100.dp)) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .combinedClickable(
                            onClickLabel = "open image",
                            onClick = {
                                if (image.uploadImageState is UploadImageState.Fail) {
                                    onDeleteFailImage(index)
                                    return@combinedClickable
                                }

                                val image = (image.uploadImageState as? UploadImageState.Success)?.image ?: return@combinedClickable
                                onOpenImage(image)
                            },
                            onLongClickLabel = "delete",
                            onLongClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onOpenDeleteDialog(index)
                            }
                        ),
                    contentScale = ContentScale.Crop,
                    model = image.lowModel(),
                    contentDescription = "image",
                )

                when (image.uploadImageState) {
                    is UploadImageState.Success -> {}
                    is UploadImageState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.Center),
                        )
                    }

                    is UploadImageState.Fail -> {
                        Icon(
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.Center),
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "fail",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}