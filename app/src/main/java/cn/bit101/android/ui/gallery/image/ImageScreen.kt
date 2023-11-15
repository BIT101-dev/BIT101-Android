package cn.bit101.android.ui.gallery.image

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter

@Composable
fun ImageScreen(url: String) {
    // 使用一个可缩放的图片
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    SubcomposeAsyncImage(
        model = url,
        contentDescription = "image",
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            offset = if (scale > 1) {
                                Offset(
                                    x = (offset.x + pan.x * scale).coerceIn(
                                        -painter.intrinsicSize.width * scale / 2,
                                        painter.intrinsicSize.width * scale / 2
                                    ),
                                    y = (offset.y + pan.y * scale).coerceIn(
                                        -painter.intrinsicSize.height * scale / 2,
                                        painter.intrinsicSize.height * scale / 2
                                    )
                                )
                            } else {
                                Offset(0f, 0f)
                            }
                        }
                    }
            ) {
                Image(
                    painter = it.painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = maxOf(1f, minOf(2f, scale)),
                            scaleY = maxOf(1f, minOf(2f, scale)),
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .clip(shape = MaterialTheme.shapes.medium)
                )
            }
        }
    )

    // 绘制UI

}