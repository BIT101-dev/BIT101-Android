package cn.bit101.android.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cn.bit101.android.database.CourseScheduleEntity
import cn.bit101.android.viewmodel.ScheduleViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * @author flwfdd
 * @date 15/05/2023 00:32
 * @description _(:з」∠)_
 */

// 课程表主界面
@Composable
fun CourseScheduleCalendar(vm: ScheduleViewModel, onConfig: () -> Unit = {}) {
    val courses by vm.courses.collectAsState()
    val week by vm.weekFlow.collectAsState(initial = Int.MAX_VALUE)
    val firstDay by vm.firstDayFlow.collectAsState(initial = null)
    val showDivider by vm.showDivider.collectAsState(initial = false)
    val showSaturday by vm.showSaturday.collectAsState(initial = true)
    val showSunday by vm.showSunday.collectAsState(initial = true)
    val showHighlightToday by vm.showHighlightToday.collectAsState(initial = true)
    val showBorder by vm.showBorder.collectAsState(initial = true)
    val timeTable by vm.timeTableFlow.collectAsState(initial = emptyList())
    val courseNumOfDay = timeTable.size

    // 课程详情弹窗
    val showCourseDetailDialog = remember { mutableStateOf(false) }
    var courseDetailData: CourseScheduleEntity? by remember { mutableStateOf(null) }
    if (showCourseDetailDialog.value && courseDetailData != null) {
        CourseScheduleDetailDialog(course = courseDetailData!!, showDialog = showCourseDetailDialog)
    }

    // 防止加载过程中闪动
    if (courses.isEmpty() || week == Int.MAX_VALUE || firstDay == null || timeTable.isEmpty()) return
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it },
        contentAlignment = Alignment.BottomEnd
    ) {
        Box {
            // 节次分割线
            Column {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Text( //空白占位
                        text = " \n ",
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75)
                    )
                }
                for (i in 1..courseNumOfDay) {
                    if (showDivider && i != 1)
                        Divider(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // 显示当前时间
            if (vm.showCurrentTime.collectAsState(initial = true).value) {
                val now = LocalTime.now()
                var a = 0f
                timeTable.forEach {
                    if (it.startTime.isBefore(now)) {
                        a += if (it.endTime.isBefore(now)) 1f
                        else (now.toSecondOfDay() - it.startTime.toSecondOfDay()).toFloat() / (it.endTime.toSecondOfDay() - it.startTime.toSecondOfDay()).toFloat()
                    }
                }
                if (a != 0f && a != courseNumOfDay.toFloat()) {
                    Column {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Text( //空白占位
                                text = " \n ",
                                style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75)
                            )
                        }
                        Spacer(modifier = Modifier.weight(a))
                        Divider(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.weight(courseNumOfDay.toFloat() - a))
                    }
                }
            }

            Column {
                // 主界面
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    // 左侧栏 周次+节次
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 显示周次
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 20.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f)
                                        )
                                    )
                                )
                        ) {
                            Text(
                                text = "${week}\n周",
                                style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 遍历显示节次
                        for (i in 1..courseNumOfDay) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "$i", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // 遍历每一天
                    courses.forEachIndexed { index, it ->
                        if (!showSaturday && index == 5) return@forEachIndexed
                        if (!showSunday && index == 6) return@forEachIndexed
                        // 计算星期和日期
                        val day = firstDay?.plusDays((week - 1) * 7 + index.toLong())
                        // 高亮今日
                        var containerColor = MaterialTheme.colorScheme.secondaryContainer
                        var columnModifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                        if (showHighlightToday && day?.equals(LocalDate.now()) == true) {
                            containerColor =
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f)
                            columnModifier = columnModifier.background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(0.25f)
                            )
                        }
                        Column(
                            modifier = columnModifier,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                containerColor,
                                                containerColor.copy(
                                                    alpha = 0f
                                                )
                                            )
                                        )
                                    )
                            ) {
                                Text(
                                    text = ("周${index + 1}\n" + ((day?.format(
                                        DateTimeFormatter.ofPattern(
                                            "MM/dd"
                                        )
                                    )) ?: "")),
                                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            var i = 1 // 节次游标
                            it.forEach {
                                if (it.start_section >= i && it.end_section <= courseNumOfDay) {
                                    if (it.start_section > i) {
                                        Spacer(modifier = Modifier.weight((it.start_section - i).toFloat()))
                                    }
                                    var modifier = Modifier
                                        .fillMaxWidth()
                                        .weight((it.end_section - it.start_section + 1).toFloat())
                                    // 是否显示边框
                                    if (showBorder) {
                                        modifier = modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                                            shape = CardDefaults.shape
                                        )
                                    }
                                    CourseScheduleItem(
                                        modifier = modifier
                                            .clip(CardDefaults.shape) // 使点击波纹形状匹配
                                            .clickable {
                                                courseDetailData = it
                                                showCourseDetailDialog.value = true
                                            },
                                        course = it
                                    )
                                    i = it.end_section + 1
                                }
                            }
                            if (i <= courseNumOfDay) {
                                Spacer(modifier = Modifier.weight(courseNumOfDay - i + 1f))
                            }
                        }
                    }
                }
            }
        }

        // 可拖动的悬浮菜单按钮
        var favOffsetY by rememberSaveable { mutableStateOf(0f) }
        val fabPaddingVerticalDp = 20.dp // 垂直边距
        val fabPaddingVerticalPx = LocalDensity.current.run { fabPaddingVerticalDp.toPx() }
        val fabPaddingHorizontalDp = 10.dp // 水平边距
        val fabSpacerSize = 10.dp // 按钮间距
        val fabSize = 42.dp
        var fabsSize by remember { mutableStateOf(IntSize.Zero) }
        Column(
            modifier = Modifier
                .onSizeChanged { fabsSize = it }
                .offset {
                    IntOffset(
                        -fabPaddingHorizontalDp
                            .toPx()
                            .toInt(),
                        -fabPaddingVerticalDp
                            .toPx()
                            .toInt() + favOffsetY.roundToInt()
                    )
                }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        favOffsetY += delta
                        if (favOffsetY > 0) favOffsetY = 0f
                        if (favOffsetY < -boxSize.height + fabPaddingVerticalPx * 2 + fabsSize.height)
                            favOffsetY =
                                -boxSize.height + fabPaddingVerticalPx * 2 + fabsSize.height
                    }
                ),
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .size(fabSize),
                onClick = {
                    vm.changeWeek(week + 1)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "next week",
                )
            }
            Spacer(modifier = Modifier.height(fabSpacerSize))
            FloatingActionButton(
                modifier = Modifier
                    .size(fabSize),
                onClick = {
                    vm.changeWeek(week - 1)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "last week",
                )
            }
            Spacer(modifier = Modifier.height(fabSpacerSize))
            FloatingActionButton(
                modifier = Modifier
                    .size(fabSize),
                onClick = {
                    onConfig()
                },
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

