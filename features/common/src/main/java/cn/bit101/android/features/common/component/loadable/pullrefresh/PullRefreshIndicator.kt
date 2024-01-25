/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.bit101.android.features.common.component.loadable.pullrefresh

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * The default indicator for Compose pull-to-refresh, based on Android's SwipeRefreshLayout.
 *
 * @sample androidx.compose.material.samples.PullRefreshSample
 *
 * @param refreshing A boolean representing whether a refresh is occurring.
 * @param state The [PullRefreshState] which controls where and how the indicator will be drawn.
 * @param modifier Modifiers for the indicator.
 * @param backgroundColor The color of the indicator's background.
 * @param contentColor The color of the indicator's arc and arrow.
 * @param scale A boolean controlling whether the indicator's size scales with pull progress or not.
 */
// TODO(b/244423199): Consider whether the state parameter should be replaced with lambdas to
//  enable people to use this indicator with custom pull-to-refresh components.
@Composable
internal fun PullRefreshIndicator(
    refreshing: Boolean,
    state: PullRefreshState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    scale: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pullRefreshIndicatorTransform(state, scale)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Crossfade(
            targetState = refreshing,
            animationSpec = tween(durationMillis = CrossfadeDurationMs),
            label = "",
        ) { refreshing ->
            val spinnerSize = IndicatorSize

            if (refreshing) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = StrokeWidth,
                    modifier = Modifier.size(spinnerSize),
                )
            } else {
                CircularArrowIndicator(state, contentColor, Modifier.size(spinnerSize))
            }
        }
        Spacer(modifier = Modifier.padding(6.dp))
        AnimatedContent(
            targetState = if(state.refreshing) "正在刷新" else if (state.progress >= 1f) "释放刷新" else "下拉刷新",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "indicator text"
        ) {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

/**
 * Modifier.size MUST be specified.
 */
@Composable
private fun CircularArrowIndicator(
    state: PullRefreshState,
    color: Color,
    modifier: Modifier,
) {
    val targetRotation by remember(state) {
        derivedStateOf {
            if (state.progress >= 1f) 180f else 0f
        }
    }

    val rotationState by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = RotateTween,
        label = "pull refresh indicator rotation"
    )

    Icon(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotationState
            },
        imageVector = Icons.Rounded.ArrowDownward,
        tint = color,
        contentDescription = null
    )

}

private const val CrossfadeDurationMs = 100

private val IndicatorSize = 20.dp
private val StrokeWidth = 2.5.dp

private val RotateTween = tween<Float>(300)
