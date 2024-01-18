package cn.bit101.android.ui.component.image

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
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

@OptIn(ExperimentalLayoutApi::class)
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

        val viewWidth = LocalDensity.current.run {
            LocalView.current.width.toDp()
        }

        var width by remember { mutableStateOf(viewWidth) }
        var placed by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            lines.forEachIndexed { idx, line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    line.forEachIndexed { index, image ->
                        BoxWithConstraints(
                            modifier = Modifier
                                .onPlaced {
                                    if(idx == lines.size - 1 && index == line.size - 1) {
                                        placed = true
                                    }
                                }
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            width = minOf(width, maxWidth)
                            PreviewImage(
                                image = image,
                                onClick = { onClick(idx * newMaxCountInEachRow + index) },
                                size = if(placed) width else maxWidth,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }

//        FlowRow(
//            modifier = modifier,
//            maxItemsInEachRow = newMaxCountInEachRow,
//            horizontalArrangement = Arrangement.spacedBy(padding),
//            verticalArrangement = Arrangement.spacedBy(4.dp),
//        ) {
//            images.forEachIndexed { index, image ->
//                PreviewImage(
//                    image = image,
//                    onClick = { onClick(index) },
//                    size = (width - padding.toPx() * (newMaxCountInEachRow - 1)) / newMaxCountInEachRow.toDp(),
//                )
//            }
//        }
    }
}