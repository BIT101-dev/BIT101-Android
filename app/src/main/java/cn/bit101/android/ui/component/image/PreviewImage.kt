package cn.bit101.android.ui.component.image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.api.model.common.Image
import coil.compose.AsyncImage

@Composable
fun PreviewImage(
    image: Image,
    onClick: () -> Unit = {},
    size: Dp = 150.dp,
) {
    AsyncImage(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            },
        contentScale = ContentScale.Crop,
        model = image.lowUrl,
        contentDescription = "image",
    )
}

@Composable
fun PreviewImage(
    modifier: Modifier = Modifier,
    image: Image,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: () -> Unit = {},
) {
    AsyncImage(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            },
        contentScale = contentScale,
        model = image.lowUrl,
        contentDescription = "image",
    )
}

@Composable
fun PreviewImages(
    modifier: Modifier = Modifier,
    images: List<Image>,
    maxCount: Int? = null,
    size: Dp = 150.dp,

    onClick: (Int) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        images.forEachIndexed { index, image ->
            if(maxCount == null || index < maxCount) {
                item(index) {
                    PreviewImage(
                        image = image,
                        onClick = { onClick(index) },
                        size = size,
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                }
            } else if(index > maxCount) {
                return@forEachIndexed
            }
            else {
                // 省略号
                item(index) {
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = "...",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewImagesWithGridLayout(
    modifier: Modifier = Modifier,
    images: List<Image>,
    maxCountInEachRow: Int,

    onClick: (Int) -> Unit = {},
) {
    val width = LocalDensity.current.run {
        LocalView.current.width - 24.dp.toPx() - 1.dp.toPx()
    }

    val newMaxCountInEachRow = if(images.size < maxCountInEachRow) images.size
    else maxOf(maxCountInEachRow, 2)

    if(images.size == 1) {
        PreviewImage(
            modifier = modifier.fillMaxWidth(),
            image = images[0],
            contentScale = ContentScale.FillWidth,
            onClick = { onClick(0) },
        )
    } else {
        val padding = 4.dp

        FlowRow(
            modifier = modifier,
            maxItemsInEachRow = maxCountInEachRow,
            horizontalArrangement = Arrangement.spacedBy(padding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            images.forEachIndexed { index, image ->
                PreviewImage(
                    image = image,
                    onClick = { onClick(index) },
                    size = LocalDensity.current.run {
                        (width.toDp() - padding * (newMaxCountInEachRow - 1)) / newMaxCountInEachRow
                    }
                )
            }
        }
    }
}