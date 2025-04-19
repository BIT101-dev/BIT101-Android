package cn.bit101.android.features.common.component.image

import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.component.DialogLayout
import cn.bit101.android.features.common.component.ImageWrapper
import cn.bit101.android.features.common.component.bottomsheet.CoreBottomSheetDefaults
import cn.bit101.android.features.common.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.features.common.helper.ImageData
import cn.bit101.android.features.common.helper.highModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageScope
import java.util.UUID
import kotlin.math.sqrt


/**
 * 显示图片，添加缩放功能
 */
@Composable
private fun ImageContent(
    painter: Painter,
    viewSize: IntSize,
    transform: ImageTransform,
    onTransform: (ImageTransform) -> Unit,
) {
    // 图片的长和宽
    val width = painter.intrinsicSize.width
    val height = painter.intrinsicSize.height

    // 显示区域的长和宽
    val showWidth = viewSize.width
    val showHeight = viewSize.height

    // 图片的初始显示倍数
    val initScale = minOf(showWidth / width, showHeight / height)

    // 最大的显示倍数
    val maxScale = 10 / initScale

    val scaleState = rememberTransformableState(
        onTransformation = { zoomChange, offsetChange, _ ->

            var newScale = transform.scale
            var newOffset = transform.offset


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

            onTransform(ImageTransform(newScale, newOffset))
        }
    )
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .transformable(scaleState)
            .graphicsLayer(
                scaleX = transform.scale,
                scaleY = transform.scale,
                translationX = transform.offset.x,
                translationY = transform.offset.y
            )
    )
}

/**
 * 加载并展示图片
 */
@Composable
internal fun ImageWithModel(
    modifier: Modifier = Modifier,
    image: ImageShowState,

    loadingContent: @Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State.Loading) -> Unit = {
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
    errorContent: @Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State.Error) -> Unit = {
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
    }
) {
    val viewWidth = LocalView.current.width
    val viewHeight = LocalView.current.height

    var size by remember { mutableStateOf(IntSize(viewWidth, viewHeight)) }

    LaunchedEffect(Unit) {
        image.transform = ImageTransform.Default
    }

    SubcomposeAsyncImage(
        modifier = modifier
            .onGloballyPositioned {
                // 获取组件的长和宽
                size = it.size
            },
        model = image.image.highModel(),
        contentDescription = "image",
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.High,
        loading = loadingContent,
        error = errorContent,
        success = {
            ImageContent(
                painter = painter,
                transform = image.transform,
                viewSize = size,
                onTransform = { image.transform = it },
            )
        }
    )
}

/**
 * 基本的图片展示界面
 */
@Composable
private fun BasicImageScreen(
    modifier: Modifier = Modifier,
    state: BasicImageShowState,
    behaviors: DialogSheetBehaviors = CoreBottomSheetDefaults.dialogSheetBehaviors(),
    controller: @Composable BoxScope.() -> Unit = {},
    imageContent: @Composable BoxScope.() -> Unit,
    onDismiss: () -> Unit,
) {
    val view = LocalView.current
    val composition = rememberCompositionContext()

    val layoutDirection = LocalLayoutDirection.current

    val dialogId = rememberSaveable { UUID.randomUUID() }

    val (finalModifier, finalBehaviors) = remember(modifier, behaviors) {
        if (behaviors.dialogWindowSoftInputMode ==
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
        ) {
            modifier to behaviors.copy(
                dialogWindowSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
            )
        } else {
            modifier to behaviors
        }
    }

    val dialog = remember(view, state) {
        ImageWrapper(
            onDismissRequest = onDismiss,
            behaviors = finalBehaviors,
            composeView = view,
            layoutDirection = layoutDirection,
            dialogId = dialogId,
        )
    }.apply {
        setContent(composition) {
            DialogLayout {
                Surface(
                    modifier = finalModifier,
                    color = MaterialTheme.colorScheme.scrim,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        imageContent()
                        controller()
                    }
                }
            }
        }
    }

    LaunchedEffect(dialog.isShowing) {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    DisposableEffect(state) {
        onDispose {
            dialog.dismiss()
            dialog.disposeComposition()
        }
    }

    SideEffect {
        dialog.updateParameters(
            onDismissRequest = onDismiss,
            behaviors = finalBehaviors,
            layoutDirection = layoutDirection
        )
    }
}


/**
 * 展示单张图片
 */
@Composable
internal fun ImageScreen(
    image: ImageData,

    onOpenUrl: (String, ()->Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberImageShowState(
        image = image,
    )
    BasicImageScreen(
        state = state,
        controller = {
            ImageController(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
                    .clip(CircleShape)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                state = state,
                onOpenUrl = onOpenUrl
            )
        },
        imageContent = {
            ImageWithModel(
                modifier = Modifier.fillMaxSize(),
                image = state,
            )
        },
        onDismiss = onDismiss,
    )
}

/**
 * 展示多张图片
 */
@Composable
internal fun ImageScreen(
    images: List<ImageData>,
    initialIndex: Int,

    onOpenUrl: (String, ()->Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberSeriesImagesShowState(
        images = images,
        initialIndex = initialIndex
    )

    BasicImageScreen(
        state = state,
        controller = {
            ImageController(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
                    .clip(CircleShape)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                state = state,
                onOpenUrl = onOpenUrl,
            )
        },
        imageContent = {
            ImageWithModel(
                modifier = Modifier.fillMaxSize(),
                image = state.currentState,
            )
        },
        onDismiss = onDismiss,
    )
}