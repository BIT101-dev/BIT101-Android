package cn.bit101.android.features.common.component.image

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.api.model.common.Image
import coil.compose.AsyncImage

@Composable
fun PreviewImage(
    modifier: Modifier = Modifier,
    image: Image,
    onClick: () -> Unit = {},
    size: Dp = 150.dp,
    corner: Boolean = true,
) {
    AsyncImage(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(if(corner) 4.dp else 0.dp))
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
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        images.forEachIndexed { index, image ->
            if(maxCount == null || index < maxCount) {
                PreviewImage(
                    image = image,
                    onClick = { onClick(index) },
                    size = size,
                )
                Spacer(modifier = Modifier.padding(2.dp))
            } else if(index > maxCount) {
                return@forEachIndexed
            }
            else {
                // 省略号
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = "...",
                )
            }
        }
    }
}

@Composable
fun PreviewImagesWithGridLayout(
    modifier: Modifier = Modifier,
    images: List<Image>,
    maxCountInEachRow: Int,

    onClick: (Int) -> Unit = {},
) {
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
        val lines = mutableListOf<List<Image>>()
        for(i in images.indices step newMaxCountInEachRow) {
            lines.add(images.subList(i, minOf(i + newMaxCountInEachRow, images.size)))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            lines.forEachIndexed { idx, line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    line.forEachIndexed { index, image ->
                        BoxWithConstraints(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            PreviewImage(
                                modifier = Modifier.align(Alignment.CenterStart),
                                image = image,
                                onClick = { onClick(idx * newMaxCountInEachRow + index) },
                                size = maxWidth,
                            )
                        }
                    }
                }
            }
        }
    }
}