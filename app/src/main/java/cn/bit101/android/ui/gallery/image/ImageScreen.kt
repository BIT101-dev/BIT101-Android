package cn.bit101.android.ui.gallery.image

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlin.math.sqrt


@Composable
fun ImageContent(
    url: String,
    scale: Float,
    offset: Offset,

    onScale: (Float) -> Unit,
    onOffset: (Offset) -> Unit,
) {
    val viewWidth = LocalView.current.width
    val viewHeight = LocalView.current.height

    var size by remember { mutableStateOf(IntSize(viewWidth, viewHeight)) }

    SubcomposeAsyncImage(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                // 获取组件的长和宽
                size = it.size
            },
        model = url,
        contentDescription = "image",
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.High,
        loading = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .width(64.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        error = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .width(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = "error",
                    modifier = Modifier.width(64.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        success = {
            // 图片的长和宽
            val width = painter.intrinsicSize.width
            val height = painter.intrinsicSize.height

            // 显示区域的长和宽
            val showWidth = size.width
            val showHeight = size.height

            // 图片的初始显示倍数
            val initScale = minOf(showWidth / width, showHeight / height)

            // 最大的显示倍数
            val maxScale = 10 / initScale

            val scaleState = rememberTransformableState(
                onTransformation = { zoomChange, offsetChange, _ ->

                    var newScale = scale
                    var newOffset = offset


                    // 以屏幕中心为中心进行缩放
                    if(newScale * zoomChange in 0.8f..maxScale) {
                        newScale *= zoomChange
                        newOffset = Offset(
                            x = newOffset.x * zoomChange,
                            y = newOffset.y * zoomChange,
                        )
                    }

                    // 移动
                    newOffset = Offset(
                        x = newOffset.x + offsetChange.x * maxOf(sqrt(newScale), 1.0f),
                        y = newOffset.y + offsetChange.y * maxOf(sqrt(newScale), 1.0f),
                    )

                    // 限制移动范围，屏幕中心部分必须在图片内
                    newOffset = if(newScale > 1.01f) Offset(
                        x = newOffset.x.coerceIn(
                            -width * initScale * newScale / 2,
                            width * initScale * newScale / 2
                        ),
                        y = newOffset.y.coerceIn(
                            -height * initScale * newScale / 2,
                            height * initScale * newScale / 2
                        )
                    ) else Offset(0.0f, 0.0f)

                    onScale(newScale)
                    onOffset(newOffset)
                }
            )
            Image(
                painter = it.painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(scaleState)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        }
    )
}

@Composable
fun ImageScreen(url: String) {
    val ctx = LocalContext.current

    // 使用一个可缩放的图片
    // 初始时短边fillMax
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset(0.0f, 0.0f)) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        ImageContent(
            url = url,
            scale = scale,
            offset = offset,
            onScale = { scale = it },
            onOffset = { offset = it },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 16.dp)
                .clip(CircleShape)
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            horizontalArrangement = Arrangement.Center
        ) {
            // 下载按钮
            IconButton(
                onClick = {
                    // 下载
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    ctx.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "download",
                )
            }
        }
    }

}


@Composable
fun ImageScreen(
    index: Int,
    urls: List<String>
) {

    if(urls.isEmpty()) return

    val ctx = LocalContext.current

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset(0.0f, 0.0f)) }

    var page by remember { mutableIntStateOf(index.coerceIn(0, urls.size - 1)) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ImageContent(
                url = urls[page],
                scale = scale,
                offset = offset,
                onScale = { scale = it },
                onOffset = { offset = it },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
                    .clip(CircleShape)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                horizontalArrangement = Arrangement.Center
            ) {
                // 上一张
                IconButton(
                    onClick = {
                        scale = 1.0f
                        offset = Offset(0.0f, 0.0f)
                        page = (page - 1 + urls.size) % urls.size
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = "previous",
                    )
                }

                Spacer(modifier = Modifier.padding(4.dp))

                // 下载按钮
                IconButton(
                    onClick = {
                        // 下载
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(urls[page])
                        ctx.startActivity(intent)
                    },

                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "download",
                    )
                }

                Spacer(modifier = Modifier.padding(4.dp))

                // 下一张
                IconButton(
                    onClick = {
                        scale = 1.0f
                        offset = Offset(0.0f, 0.0f)
                        page = (page + 1) % urls.size
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = "next",
                    )
                }

                Spacer(modifier = Modifier.padding(4.dp))

                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = "${page + 1}/${urls.size}",
                )
            }

        }
    }
}