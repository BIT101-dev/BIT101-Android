package cn.bit101.android.ui.component.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.common.ImageData

@Composable
fun ImageController(
    modifier: Modifier,
    state: SeriesImagesShowState,
    onOpenUrl: (String) -> Unit,
) {

    val currentState = state.currentState

    val url = when(currentState.image) {
        is ImageData.Remote -> currentState.image.image.url
        is ImageData.RemoteUseUrl -> currentState.image.url
        else -> null
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        // 上一张
        IconButton(
            onClick = {
                state.currentState.resetTransform()
                state.currentIndex = (state.currentIndex - 1 + state.size) % state.size
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                contentDescription = "previous",
            )
        }

        Spacer(modifier = Modifier.padding(4.dp))

        // 下载按钮
        if(url != null) {
            IconButton(onClick = { onOpenUrl(url) }) {
                Icon(imageVector = Icons.Rounded.Download, contentDescription = "download")
            }
        }

        Spacer(modifier = Modifier.padding(4.dp))

        // 下一张
        IconButton(
            onClick = {
                state.currentState.resetTransform()
                state.currentIndex = (state.currentIndex + 1) % state.size
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
            text = "${state.currentIndex + 1}/${state.size}",
        )
    }
}


@Composable
fun ImageController(
    modifier: Modifier,
    state: ImageShowState,
    onOpenUrl: (String) -> Unit,
) {
    val url = when(state.image) {
        is ImageData.Remote -> state.image.image.url
        is ImageData.RemoteUseUrl -> state.image.url
        else -> null
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        // 下载按钮
        if(url != null) {
            IconButton(onClick = { onOpenUrl(url) }) {
                Icon(imageVector = Icons.Rounded.Download, contentDescription = "download")
            }
        }
    }
}