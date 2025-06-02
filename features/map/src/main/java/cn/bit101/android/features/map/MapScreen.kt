package cn.bit101.android.features.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ovh.plrapps.mapcompose.ui.MapUI

@Composable
fun MapScreen() {
    val vm: MapViewModel = hiltViewModel()

    val scale by vm.mapScaleFlow.collectAsState(initial = 2f)

    Box {
        // 地图界面
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            MapUI(
                modifier = Modifier
                    .fillMaxSize(1f / scale)
                    .scale(scale),
                state = vm.state,
            )
        }

        // 第否显示底部设置栏
        var showBottomBar by rememberSaveable { mutableStateOf(false) }

        // 浮动按钮
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 20.dp)
                .statusBarsPadding(),
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
                    vm.scrollTo(vm.LiangXiang)
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
                    vm.scrollTo(vm.ZhongGuanCun)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text("村")
            }
        }


        // 底部设置栏
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
                            onValueChange = { vm.setMapScale(it) },
                            valueRange = 1f..5f,
                            steps = 7,
                        )
                    }
                }
            }
        }
    }
}