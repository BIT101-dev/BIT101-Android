package cn.bit101.android.ui.schedule

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.MainController
import cn.bit101.android.database.CourseScheduleEntity
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.viewmodel.ScheduleViewModel
import cn.bit101.android.viewmodel.getTermsFromNet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * @author flwfdd
 * @date 2023/4/12 14:29
 * @description _(:з」∠)_
 */

@Composable
fun CourseSchedule(
    mainController: MainController,
    active: Boolean,
    vm: ScheduleViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var showDialog by rememberSaveable { mutableStateOf(false) }

        CourseScheduleCalendar(vm, onConfig = { showDialog = true })

        // 编辑对话框 自定义进入和退出动画
        AnimatedVisibility(
            visible = showDialog,
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
            ConfigDialog(mainController, vm) {
                showDialog = false
            }
        }

        // 响应返回键
        BackHandler(enabled = showDialog && active) {
            showDialog = false
        }
    }
}

// 课程表主界面
@Composable
fun CourseScheduleCalendar(vm: ScheduleViewModel, onConfig: () -> Unit = {}) {
    val courses by vm.courses.collectAsState()
    val week = vm.weekFlow.collectAsState().value
    val firstDay by vm.firstDayFlow.collectAsState(initial = null)
    val showDivider by vm.showDivider.collectAsState(initial = true)
    val showSaturday by vm.showSaturday.collectAsState(initial = true)
    val showSunday by vm.showSunday.collectAsState(initial = true)
    val showHighlightToday by vm.showHighlightToday.collectAsState(initial = true)
    val showBorder by vm.showBorder.collectAsState(initial = true)

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
                    Text(text = "\n", style = MaterialTheme.typography.labelSmall)
                }
                for (i in 1..13) {
                    Spacer(modifier = Modifier.weight(1f))
                    if (showDivider)
                        Divider(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                }
            }

            Column {
                // 主界面
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Column( //节次
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
                        for (i in 1..13) {
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
                        var md = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                        if (showHighlightToday && day?.equals(LocalDate.now()) == true) {
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            md = md.background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(0.25f)
                            )
                        }
                        Column(
                            modifier = md,
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
                                if (it.start_section >= i) {
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
                                    CourseCard(
                                        modifier = modifier,
                                        course = it
                                    )
                                    i = it.end_section + 1
                                }
                            }
                            if (i <= 13) {
                                Spacer(modifier = Modifier.weight(13 - i + 1f))
                            }
                        }
                    }
                }
            }
        }

        // 可拖动的悬浮菜单按钮
        var favOffsetY by remember { mutableStateOf(0f) }
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
                elevation= FloatingActionButtonDefaults.elevation(0.dp),
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
                elevation= FloatingActionButtonDefaults.elevation(0.dp),
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
                elevation= FloatingActionButtonDefaults.elevation(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "settings",
                )
            }
        }
    }
}


// 单个课程卡片
@Composable
fun CourseCard(modifier: Modifier, course: CourseScheduleEntity) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = Math.random().toFloat() * 0.5f + 0.5f
            )
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = course.classroom,
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }
}


// 课表设置对话框
@Composable
fun ConfigDialog(mainController: MainController, vm: ScheduleViewModel, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "课表设置", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { onDismiss() }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "close config dialog",
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val showTermListDialog = rememberSaveable { mutableStateOf(false) }
        val term = vm.termFlow.collectAsState(initial = null).value
        var refreshing by remember { mutableStateOf(false) }
        ConfigColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            items = listOf(
                ConfigItem.Button(
                    title = "刷新当前学期课表",
                    onClick = {
                        MainScope().launch {
                            if (term == null) {
                                mainController.snackbar("未选择学期")
                                return@launch
                            }
                            if (!refreshing) {
                                refreshing = true
                                vm.changeTerm(
                                    term = term,
                                    onSuccess = {
                                        mainController.snackbar("刷新成功")
                                        refreshing = false
                                    },
                                    onFail = {
                                        mainController.snackbar("刷新失败")
                                        refreshing = false
                                    }
                                )
                            }
                        }
                    }
                ),
                ConfigItem.Button(
                    title = "切换学期",
                    content = term ?: "未选择",
                    onClick = {
                        showTermListDialog.value = true
                    }
                ),
                ConfigItem.Switch(
                    title = "显示周六",
                    checked = vm.showSaturday.collectAsState(initial = true).value,
                    onCheckedChange = {
                        vm.setShowSaturday(it)
                    }
                ),
                ConfigItem.Switch(
                    title = "显示周日",
                    checked = vm.showSunday.collectAsState(initial = true).value,
                    onCheckedChange = {
                        vm.setShowSunday(it)
                    }
                ),
                ConfigItem.Switch(
                    title = "显示边框",
                    checked = vm.showBorder.collectAsState(initial = true).value,
                    onCheckedChange = {
                        vm.setShowBorder(it)
                    }
                ),
                ConfigItem.Switch(
                    title = "高亮今日",
                    checked = vm.showHighlightToday.collectAsState(initial = true).value,
                    onCheckedChange = {
                        vm.setShowHighlightToday(it)
                    }
                ),
                ConfigItem.Switch(
                    title = "显示节次分割线",
                    checked = vm.showDivider.collectAsState(initial = true).value,
                    onCheckedChange = {
                        vm.setShowDivider(it)
                    }
                ),
            ))

        if (showTermListDialog.value) {
            TermListDialog(mainController, vm, showTermListDialog)
        }
    }
}

// 选择学期对话框
@Composable
fun TermListDialog(
    mainController: MainController,
    vm: ScheduleViewModel,
    showTermListDialog: MutableState<Boolean>
) {
    var termList by remember { mutableStateOf(listOf("")) }
    val (selectedOption, onOptionSelected) = rememberSaveable { mutableStateOf("") }
    LaunchedEffect(showTermListDialog) {
        termList = getTermsFromNet()
        if (termList.isNotEmpty()) onOptionSelected(termList[0])
    }
    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.75f),
        onDismissRequest = {
            showTermListDialog.value = false
        },
        title = {
            Text(text = "切换学期")
        },
        text = {
            if (selectedOption.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else {
                val scrollState = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxSize()
                        .selectableGroup()
                        .verticalScroll(scrollState)
                ) {
                    termList.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = { onOptionSelected(text) },
                                    role = Role.RadioButton
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = null
                            )
                            Text(
                                text = text,
                                modifier = Modifier.padding(start = 10.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.changeTerm(
                        term = selectedOption,
                        onSuccess = {
                            mainController.snackbar("成功切换至 $selectedOption")
                        },
                        onFail = {
                            mainController.snackbar("切换失败")
                        }
                    )
                    showTermListDialog.value = false
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showTermListDialog.value = false
                }
            ) {
                Text("取消")
            }
        }
    )
}