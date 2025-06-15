package cn.bit101.android.features.schedule.classroom

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.common.utils.getCurrentTime
import cn.bit101.android.features.common.utils.mixColor
import cn.bit101.android.features.schedule.classroom.FreeClassroomSearchViewModel.ClassroomBusyData
import cn.bit101.api.model.common.BuildingInfo
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*

private fun formatSecondToString(second: Int): String {
    return when (second) {
        in 0..<60 -> "< 1 分钟"
        in 60..<60 * 60 -> "${second / 60} 分钟"
        in 60 * 60..Int.MAX_VALUE ->
            "${
                second / 60 / 60
            } 小时${
                if((second / 60) % 60 != 0)
                    " ${(second / 60) % 60} 分钟"
                else
                    ""
            }"
        else -> "-${formatSecondToString(-second)}" // 按理来说不会发生
    }
}

@Composable
internal fun ClassroomList(
    currentClassrooms: List<ClassroomBusyData>,
    nowTime: LocalTime,
    isFreeNow: (ClassroomBusyData) -> Boolean
) {
    currentClassrooms
        .forEach { item ->
            val restSeconds = (item.nextBusyTime.toSecondOfDay() - nowTime.toSecondOfDay())

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 5.dp)
                    .clip(MaterialTheme.shapes.medium),
                color =
                if(isFreeNow(item))
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    mixColor(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                        0.5f
                    ),
                contentColor =
                if(isFreeNow(item))
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    mixColor(
                        MaterialTheme.colorScheme.onErrorContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                        0.5f
                    ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 15.dp, 5.dp, 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = item.classroom.classroomName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "空闲时段: ${item.prettyFreeTimes}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                    ){
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = when (item.nextBusyTime) {
                                LocalTime.MAX -> "空闲到明天"
                                else -> {
                                    if(restSeconds > 0)
                                        "还会空闲 ${formatSecondToString(restSeconds)}"
                                    else if(item.nextFreeTime != null)
                                        "${
                                            formatSecondToString(
                                                item.nextFreeTime.toSecondOfDay() - nowTime.toSecondOfDay()
                                            )
                                        } 后空闲"
                                    else
                                        "使用中"
                                }
                            }
                        )
                        if(item.nextBusyTime!=LocalTime.MAX)
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text =
                                if(restSeconds > 0)
                                    "(直到 ${item.nextBusyTime})"
                                else if(item.nextFreeTime!=null)
                                    "(${item.nextFreeTime})"
                                else ""
                            )
                    }
                }
            }
        }
}

@Composable
internal fun BuildingItem(
    buildingInfo: BuildingInfo,
    expanded: Boolean,
    onSwitchActive: () -> Unit,
) {
    // 这里如果用 PrimaryContainer 系颜色的话, 就会和右下角的按钮完美地糊在一块, 用 SecondaryContainer 系的又会和教室完美地糊在一块
    // 于是调了半天调出了个勉强不会糊在一块的颜色
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 5.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onSwitchActive() },
        color =
        mixColor(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer,
            0.25f
        ),
        contentColor =
        mixColor(
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onPrimaryContainer,
            0.25f
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        val arrowRotateDegrees: Float by animateFloatAsState(if (expanded) 90f else 0f)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 15.dp, 5.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = buildingInfo.buildingName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = buildingInfo.campusName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .scale(1.25f)
                    .padding(horizontal = 5.dp)
                    .rotate(arrowRotateDegrees)
            )
        }
    }
}

@Composable
internal fun LoadingTip(
    tipText: String,
) {
    Box{
        Text(
            text = tipText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 5.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(10.dp, 5.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 5.dp),
        )
    }
}

@Composable
internal fun FreeClassroomSearch(
    mainController: MainController,
    active: Boolean,
    vm: FreeClassroomSearchViewModel = hiltViewModel()
) {
    val getBuildingTypeStatus by vm.getBuildingTypeStatusLiveData.observeAsState()

    val currentBuildings = (getBuildingTypeStatus as? SimpleDataState.Success)?.data ?: emptyList()

    val currentCampusCode by vm.nowCampusFlow.collectAsState(initial = null)

    val currentCampusName by vm.nowCampusNameFlow.collectAsState(initial = null)

    val getClassroomStatusMap = vm.getClassroomsStatesMap

    val currentClassroomData = vm.classroomDataMap

    val hideBusyClassroom = vm.hideBusyClassroomFlow.collectAsState(initial = false)

    val freeMinutesThreshold by vm.freeMinutesThresholdFlow.collectAsState(initial = null)

    val selectedIndices = vm.selectedIndices

    val listState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    // 非常 dirty 的解决方式
    var lastCampus by rememberSaveable { mutableStateOf(currentCampusCode) }
    LaunchedEffect(currentCampusCode) {
        // nowCampus 不可能为空, 所以为 null 的情况只能是 initial, 此时不用管
        if(currentCampusCode != null && currentCampusCode != lastCampus) {
            // 否则说明校区发生了切换
            lastCampus = currentCampusCode

            vm.loadBuildingTypes()

            vm.clearSelectState()
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(getBuildingTypeStatus) {
        if (getBuildingTypeStatus is SimpleDataState.Fail) {
            mainController.snackbar("拉取教学楼信息失败 Orz...")
        }
    }

    val getClassroomLastStatus = vm.getClassroomLastStatusLiveData.value
    LaunchedEffect(getClassroomLastStatus) {
        if (getClassroomLastStatus is SimpleState.Fail) {
            mainController.snackbar("拉取教室信息失败 Orz...")
        }
    }

    DisposableEffect(Unit) {
        if(getBuildingTypeStatus !is SimpleDataState.Success) {
            vm.loadBuildingTypes()
        }

        onDispose {
            if(getBuildingTypeStatus !is SimpleDataState.Success) {
                vm.getBuildingTypeStatusLiveData.value = null
            }
            if(getClassroomLastStatus !is SimpleState.Success) {
                vm.getClassroomLastStatusLiveData.value = null
            }
        }
    }

    var lastFreeMinutesThreshold by rememberSaveable{ mutableStateOf(freeMinutesThreshold) }
    LaunchedEffect(freeMinutesThreshold) {
        if(freeMinutesThreshold != null && freeMinutesThreshold != lastFreeMinutesThreshold) {
            lastFreeMinutesThreshold = freeMinutesThreshold
            vm.refreshAllClassroomInfo()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        val nowTime = getCurrentTime()

        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
            if (getBuildingTypeStatus is SimpleDataState.Loading) {
                item { LoadingTip("正在拉取教学楼列表...") }
            } else {
                if(currentBuildings.isEmpty()) {
                    if(!currentCampusName.isNullOrEmpty()) {
                        item {
                            Text(
                                text = "没有获取到${
                                    if (currentCampusName!!.endsWith("校区"))
                                        currentCampusName
                                    else
                                        "${currentCampusName}校区"
                                }的教学楼QwQ\n(请尝试在设置中切换校区)",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp, 5.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(10.dp, 5.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                } else {
                    itemsIndexed(currentBuildings) { index, item ->
                        val nowExpanded by remember { derivedStateOf { selectedIndices.contains(index) } }

                        BuildingItem(
                            buildingInfo = item,
                            expanded = nowExpanded,
                            onSwitchActive = {
                                if(!nowExpanded) {
                                    vm.loadClassroomInfos(item.buildingIndex)
                                }

                                vm.switchSelectState(index)
                            }
                        )

                        AnimatedVisibility(
                            visible = nowExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 25.dp)
                            ) {
                                if (getClassroomStatusMap[item.buildingIndex] is SimpleState.Loading) {
                                    LoadingTip("正在获取教室列表...")
                                } else {
                                    if (currentClassroomData.containsKey(item.buildingIndex)) {
                                        vm.refreshClassroomInfoIfInvalid(item.buildingIndex)
                                    }

                                    val currentClassrooms =
                                        currentClassroomData[item.buildingIndex]
                                            .orEmpty()
                                            .filter {
                                                !hideBusyClassroom.value || vm.isFreeNow(
                                                    it,
                                                    nowTime,
                                                    freeMinutesThreshold ?: 0
                                                )
                                            }

                                    if (currentClassrooms.isEmpty()) {
                                        Text(
                                            text = "未找到教室 :(",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp, 5.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(10.dp, 5.dp),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    } else {
                                        ClassroomList(
                                            currentClassrooms = currentClassrooms,
                                            nowTime = nowTime,
                                            isFreeNow = { vm.isFreeNow(it, nowTime, freeMinutesThreshold ?: 0) },
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        // 悬浮按钮组
        val fabSize = 42.dp
        Column(
            modifier = Modifier
                .padding(10.dp, 20.dp)
        ) {
            // 从话廊那毛过来的回到顶部按钮
            val show by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
            AnimatedVisibility(
                visible = show,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    modifier = Modifier
                        .size(fabSize),
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "回到顶部"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            // 设置按钮
            FloatingActionButton(
                modifier = Modifier
                    .size(fabSize),
                onClick = { mainController.navigate(NavDest.Setting("freeClassroom")) },
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "settings",
                )
            }
        }
    }
}