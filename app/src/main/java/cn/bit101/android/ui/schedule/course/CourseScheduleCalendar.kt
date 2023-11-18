package cn.bit101.android.ui.schedule.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cn.bit101.android.database.entity.CourseScheduleEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * @author flwfdd
 * @date 15/05/2023 00:32
 * @description 课程表日历界面
 * _(:з」∠)_
 */

@Composable
fun CourseScheduleCalendar(
    courses: List<List<CourseScheduleEntity>>,
    week: Int,
    firstDay: LocalDate,
    settingData: SettingData,
    timeTable: List<CourseScheduleViewModel.TimeTableItem>,

    onConfig: () -> Unit,
    onShowDetailDialog: (CourseScheduleEntity) -> Unit,
    onChangeWeek: (Int) -> Unit,
) {
    /**
     * 一天的节数
     */
    val courseNumOfDay = timeTable.size

    val courseTimes = timeTable.map { it.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) }

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it },
        contentAlignment = Alignment.BottomEnd
    ) {
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
                if (settingData.showDivider && i != 1)
                    HorizontalDivider(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (settingData.showCurrentTime) {
            val now = LocalTime.now()
            var topWeight = 0f // 上半部分所占比重
            timeTable.forEach {
                if (it.startTime.isBefore(now)) {
                    topWeight += if (it.endTime.isBefore(now)) 1f
                    else (now.toSecondOfDay() - it.startTime.toSecondOfDay()).toFloat() / (it.endTime.toSecondOfDay() - it.startTime.toSecondOfDay()).toFloat()
                }
            }
            if (topWeight != 0f && topWeight != courseNumOfDay.toFloat()) {
                Column {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Text( //空白占位
                            text = " \n ",
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75)
                        )
                    }
                    Spacer(modifier = Modifier.weight(topWeight))
                    HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.weight(courseNumOfDay.toFloat() - topWeight))
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
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 显示周次
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
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
                    for (i in 0 until courseNumOfDay) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${i+1}",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            if (i <= courseTimes.size) {
                                Text(
                                    text = courseTimes[i],
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = LocalContentColor.current.copy(0.8f),
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 遍历每一天
                courses.forEachIndexed { index, it ->
                    if (!settingData.showSaturday && index == 5) return@forEachIndexed
                    if (!settingData.showSunday && index == 6) return@forEachIndexed
                    // 计算星期和日期
                    val day = firstDay.plusDays((week - 1) * 7 + index.toLong())

                    // 用于高亮今日 改变颜色
                    var containerColor = MaterialTheme.colorScheme.secondaryContainer
                    var columnModifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                    if (settingData.showHighlightToday && day?.equals(LocalDate.now()) == true) {
                        containerColor =
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f)
                        columnModifier = columnModifier.background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(0.25f)
                        )
                    }

                    Column(
                        modifier = columnModifier,
                    ) {
                        // 头部星期日期
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

                        // 遍历一天的每一节课
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
                                if (settingData.showBorder) {
                                    modifier = modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                                        shape = CardDefaults.shape
                                    )
                                }
                                i = it.end_section + 1

                                CourseScheduleItem(
                                    modifier = modifier
                                        .clip(CardDefaults.shape) // 使点击波纹形状匹配
                                        .clickable { onShowDetailDialog(it) },
                                    course = it
                                )
                            }
                        }

                        // 填充剩余空白
                        if (i <= courseNumOfDay) {
                            Spacer(modifier = Modifier.weight(courseNumOfDay - i + 1f))
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
                onClick = { onChangeWeek(week + 1) },
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
                onClick = { onChangeWeek(week - 1) },
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
                onClick = onConfig,
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

