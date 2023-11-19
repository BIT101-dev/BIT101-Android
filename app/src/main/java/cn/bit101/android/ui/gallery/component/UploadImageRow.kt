package cn.bit101.android.ui.gallery.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.gallery.common.ImageData
import cn.bit101.android.ui.gallery.common.UploadImageState
import cn.bit101.api.model.common.Image
import coil.compose.AsyncImage

/**
 * 上传图片的部分
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UploadImageRow(
    /**
     * 最大图片数量
     */
    maxImages: Int = 100,

    /**
     * 图片列表
     */
    images: List<Pair<ImageData, UploadImageState>>,

    /**
     * 上传图片
     */
    onUploadImage: () -> Unit,

    /**
     * 打开图片
     */
    onOpenImage: (Image) -> Unit,

    /**
     * 打开删除图片的对话框
     */
    onOpenDeleteDialog: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(images) { index, it ->
            Box(modifier = Modifier.padding(end = 4.dp).size(100.dp)) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .combinedClickable(
                            onClick = { onOpenImage((it.second as UploadImageState.Success).image) },
                            onLongClick = { onOpenDeleteDialog(index) }
                        ),
                    contentScale = ContentScale.Crop,
                    model = if (it.second is UploadImageState.Success) (it.second as UploadImageState.Success).image.lowUrl
                    else when (it.first) {
                        is ImageData.Local -> (it.first as ImageData.Local).uri
                        is ImageData.Remote -> (it.first as ImageData.Remote).image.lowUrl
                    },
                    contentDescription = "image",
                )

                when (it.second) {
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
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "fail",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        item("upload") {
            if(images.size < maxImages) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .size(100.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onUploadImage() },
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.Center),
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "add"
                        )
                    }
                }
            }
        }
    }
}