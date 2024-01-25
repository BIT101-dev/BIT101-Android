package cn.bit101.android.features.common.component.gallery

import android.util.Log
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
private fun LikeContent(
    size: IntSize,
    offset: Offset,
    like: Boolean,
    process: Float,
) {
    val seed by remember(like) { mutableIntStateOf(LocalDateTime.now().second) }

    // 三波原点
    // 第一波半径从 0 ~ 1, primary
    // 第二波半径从 0 ~ 1.5, tertiary
    // 第三波半径从 0 ~ 1.5, primary
    // 半径加一些轻微扰动

    val maxRadius = minOf(size.width / 2f, size.height / 2f)

    val points = remember(like) { mutableListOf<Offset>() }

    Log.i("LikeContent", "size: $size, offset: $offset, like: $like, process: $process")

    val centerOffset = Offset(size.width / 2f, size.height / 2f)

    points.clear()
    for (i in 0..20) {
        val radius = process * maxRadius
        val theta = 360f / 20 * i
        val pointOffset = Offset(
            centerOffset.x + radius * cos(theta) + (Random(seed).nextFloat() - 0.5f) * 0.05f * maxRadius,
            centerOffset.y + radius * sin(theta) + (Random(seed).nextFloat() - 0.5f) * 0.05f * maxRadius,
        )
        points.add(pointOffset)
    }

    val dpSize = LocalDensity.current.run { DpSize(size.width.toDp(), size.height.toDp()) }

    val color = if(like) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary

    Canvas(modifier = Modifier.size(dpSize)) {
        drawPoints(
            points = points,
            pointMode = PointMode.Points,
            color = color,
            cap = StrokeCap.Round,
            strokeWidth = 2.dp.toPx(),
        )
    }
}

@Composable
fun LikeIcon(
    modifier: Modifier = Modifier,
    like: Boolean,
    liking: Boolean,
) {
    var lastLike by remember { mutableStateOf(like) }

    LaunchedEffect(like) {
        if(!like) {
            lastLike = false
        }
    }

    val process by animateFloatAsState(
        targetValue = if(lastLike != like) 0f else 1f,
        animationSpec = tween(200, easing = FastOutLinearInEasing),
        label = "like anim",
        finishedListener = {
            lastLike = like
        }
    )


    var size by remember { mutableStateOf(IntSize.Zero) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Icon(
        modifier = modifier.onPlaced {
            size = it.size
            offset = it.positionInWindow()
        },
        imageVector = if(like) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = "点赞",
        tint = if(liking) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        else if(like) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.onSurface
    )

//    Box {
//        Icon(
//            modifier = modifier.onPlaced {
//                size = it.size
//                offset = it.positionInWindow()
//            },
//            imageVector = if(like) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
//            contentDescription = "点赞",
//            tint = if(liking) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//            else if(like) MaterialTheme.colorScheme.tertiary
//            else MaterialTheme.colorScheme.onSurface
//        )
//        LikeContent(size = size, offset = offset, like = like, process = process)
//    }
}

@Preview
@Composable
private fun LikeIconPreview() {
    var like by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            delay(1000)
            like = true
        }
    }

    Row {
        LikeIcon(like = like, liking = false, modifier = Modifier.padding(8.dp))
    }
}