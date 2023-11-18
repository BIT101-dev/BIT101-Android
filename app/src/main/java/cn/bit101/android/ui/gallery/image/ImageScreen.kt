package cn.bit101.android.ui.gallery.image

import android.content.Intent
import android.net.Uri
import android.view.WindowMetrics
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter

@Composable
fun ImageScreen(url: String) {

    SubcomposeAsyncImage(
        modifier = Modifier.fillMaxSize(),
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

            // 使用一个可缩放的图片
            // 初始时短边fillMax
            var scale by remember { mutableFloatStateOf(1.0f) }
            var offset by remember { mutableStateOf(Offset(0f, 0f)) }

            val scaleState = rememberTransformableState(
                onTransformation = { zoomChange, offsetChange, _ ->

                    val canMulti = (scale * zoomChange >= 1.0f) && (scale * zoomChange <= 10.0f)

                    scale = (scale * zoomChange).coerceIn(
                        1.0f,
                        10.0f
                    )

                    // 以屏幕中心为中心进行缩放，移动到对应位置
                    // 保证不会移动到图片外部
                    offset = if (scale > 1) {
                        Offset(
                            x = (offset.x * if(canMulti) zoomChange else 1.0f + offsetChange.x).coerceIn(
                                -width * scale / 2,
                                width * scale / 2
                            ),
                            y = (offset.y * if(canMulti) zoomChange else 1.0f + offsetChange.y).coerceIn(
                                -height * scale / 2,
                                height * scale / 2
                            )
                        )
                    } else Offset(0f, 0f)
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

    // 绘制UI

}