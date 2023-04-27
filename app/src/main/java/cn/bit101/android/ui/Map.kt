package cn.bit101.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.viewmodel.MapViewModel
import ovh.plrapps.mapcompose.ui.MapUI

/**
 * @author flwfdd
 * @date 2023/4/27 23:45
 * @description _(:з」∠)_
 */

@Composable
fun MapComponent(
    mapVM: MapViewModel = viewModel(),
) {
    Box {
        val scale = mapVM.scaleFlow.collectAsState(initial = 2f).value //地图缩放
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            MapUI(
                Modifier
                    .fillMaxSize(1f / scale)
                    .scale(scale), state = mapVM.state
            )
        }

        var showBottomBar by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 20.dp),
        ) {
            val fabSize = 50.dp
            FloatingActionButton(
                modifier = Modifier.size(fabSize),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    showBottomBar = !showBottomBar
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "settings",
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FloatingActionButton(
                modifier = Modifier.size(fabSize),
                onClick = {
                    mapVM.scrollTo(mapVM.LiangXiang)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text("乡")
            }
            Spacer(modifier = Modifier.height(10.dp))
            FloatingActionButton(
                modifier = Modifier.size(fabSize),
                onClick = {
                    mapVM.scrollTo(mapVM.ZhongGuanCun)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text("村")
            }
        }


        val bottomBarTransitionState =
            remember { MutableTransitionState(false) }
        bottomBarTransitionState.apply { targetState = showBottomBar }

        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visibleState = bottomBarTransitionState,
                enter = slideIn(
                    initialOffset = { IntOffset(0, it.height) },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                ),
                exit = slideOut(
                    targetOffset = { IntOffset(0, it.height) },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large.copy(
                        bottomStart = ZeroCornerSize,
                        bottomEnd = ZeroCornerSize,
                    ),
                    shadowElevation = 16.dp,
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("缩放倍率 $scale", style = MaterialTheme.typography.titleMedium)
                            IconButton(
                                onClick = {
                                    showBottomBar = false
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "close",
                                )
                            }
                        }

                        Slider(
                            value = scale,
                            onValueChange = { mapVM.setScale(it) },
                            valueRange = 1f..5f,
                            steps = 7,
                        )
                    }
                }
            }
        }
    }
}