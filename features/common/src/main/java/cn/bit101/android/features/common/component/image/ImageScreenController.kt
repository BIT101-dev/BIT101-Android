package cn.bit101.android.features.common.component.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.helper.ImageData

@Composable
internal fun ImageController(
    modifier: Modifier,
    state: SeriesImagesShowState,
    onOpenUrl: (String) -> Unit,
) {

    val currentState = state.currentState

    val url = when (currentState.image) {
        is ImageData.Remote -> currentState.image.image.url
        is ImageData.RemoteUseUrl -> currentState.image.url
        else -> null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 上一张
            FilledTonalIconButton(
                onClick = {
                    state.currentState.resetTransform()
                    state.currentIndex = (state.currentIndex - 1 + state.size) % state.size
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBackIos,
                    contentDescription = "previous",
                )
            }

            // 下一张
            FilledTonalIconButton(
                onClick = {
                    state.currentState.resetTransform()
                    state.currentIndex = (state.currentIndex + 1) % state.size
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "next",
                )
            }
        }
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                text = "${state.currentIndex + 1}/${state.size}",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (url != null) {
            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                onClick = { onOpenUrl(url) }
            ) {
                Icon(imageVector = Icons.Rounded.Download, contentDescription = "download")
            }
        }
    }
}


@Composable
internal fun ImageController(
    modifier: Modifier,
    state: ImageShowState,
    onOpenUrl: (String) -> Unit,
) {
    val url = when(state.image) {
        is ImageData.Remote -> state.image.image.url
        is ImageData.RemoteUseUrl -> state.image.url
        else -> null
    }
    Box(modifier = Modifier.fillMaxSize()) {

        if (url != null) {
            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                onClick = { onOpenUrl(url) }
            ) {
                Icon(imageVector = Icons.Rounded.Download, contentDescription = "download")
            }
        }
    }
}