package cn.bit101.android.features.setting.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.config.setting.base.TimeTable
import cn.bit101.android.config.setting.base.toTimeTableString
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import cn.bit101.android.features.common.component.schedule.AddEditScheduleDialog
import cn.bit101.android.features.common.component.schedule.CustomScheduleDetailDialog
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.CalendarViewModel
import cn.bit101.android.features.setting.viewmodel.SettingData
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
private fun CalendarSettingPageContent(
    currentTerm: String,
    firstDay: String,
    settingData: SettingData,

    isGettingFirstDay: Boolean,
    isGettingCourses: Boolean,
    isGettingExams: Boolean,

    onOpenTermListDialog: () -> Unit,
    onGetFirstDay: () -> Unit,
    onGetCourses: () -> Unit,
    onGetExams: () -> Unit,
    onOpenTimeTable: () -> Unit,
    onOpenCustomSchedules: () -> Unit,
    onSettingChange: (SettingData) -> Unit,
) {
    val dataSettings = listOf(
        SettingItemData.Button(
            title = "当前学期",
            subTitle = "设置当前学期",
            onClick = onOpenTermListDialog,
            text = currentTerm
        ),
        SettingItemData.Button(
            enable = !isGettingFirstDay,
            title = "学期起始日期",
            subTitle = "点击重新获取当前学期的起始日期",
            onClick = onGetFirstDay,
            text = firstDay,
        ),

        SettingItemData.Button(
            enable = !isGettingCourses,
            title = "课表数据",
            subTitle = "点击重新获取当前学期的课表",
            onClick = onGetCourses,
        ),

        SettingItemData.Button(
            enable = !isGettingExams,
            title = "考试安排数据",
            subTitle = "点击重新获取当前学期的考试安排",
            onClick = onGetExams,
        ),

        SettingItemData.Button(
            title = "时间表",
            subTitle = "每节课的上课和下课时间\n不建议乱改, 很多模块高度依赖此表格",
            onClick = onOpenTimeTable,
        ),

        SettingItemData.Button(
            title = "自定义日程",
            subTitle = "点击查看所有自定义日程",
            onClick = onOpenCustomSchedules,
        ),
    )

    val displaySettings = listOf(
        SettingItemData.Switch(
            title = "显示周六",
            onClick = { onSettingChange(settingData.copy(showSaturday = it)) },
            checked = settingData.showSaturday,
        ),
        SettingItemData.Switch(
            title = "显示周日",
            onClick = { onSettingChange(settingData.copy(showSunday = it)) },
            checked = settingData.showSunday,
        ),
        SettingItemData.Switch(
            title = "显示课程卡片边框",
            onClick = { onSettingChange(settingData.copy(showBorder = it)) },
            checked = settingData.showBorder,
        ),
        SettingItemData.Switch(
            title = "高亮今日",
            onClick = { onSettingChange(settingData.copy(showHighlightToday = it)) },
            checked = settingData.showHighlightToday,
        ),
        SettingItemData.Switch(
            title = "显示节次分割线",
            onClick = { onSettingChange(settingData.copy(showDivider = it)) },
            checked = settingData.showDivider,
        ),
        SettingItemData.Switch(
            title = "显示当前时间线",
            onClick = { onSettingChange(settingData.copy(showCurrentTime = it)) },
            checked = settingData.showCurrentTime,
        ),
        SettingItemData.Switch(
            title = "显示考试安排",
            subTitle = "考试安排更新时需手动刷新才能更新\n仅供参考, 错过考试概不负责 XP",
            onClick = { onSettingChange(settingData.copy(showExamInfo = it)) },
            checked = settingData.showExamInfo,
        ),
    )

    SettingsColumn {
        SettingsGroup(
            title = "数据设置",
            items = dataSettings,
        )
        SettingsGroup(
            title = "显示设置",
            items = displaySettings,
        )
    }
}


// 选择学期对话框
@Composable
private fun TermListDialog(
    term: String,

    onChangeTerm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var termEdit by rememberSaveable { mutableStateOf(term) }

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "切换学期") },
        text = {
            TextField(
                value = termEdit,
                onValueChange = { termEdit = it },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onChangeTerm(termEdit) }
            ) {
                Text("确定")
            }
        },
    )
}


// 设置时间表对话框
@Composable
private fun TimeTableDialog(
    timeTable: TimeTable,
    errorMessage: String,
    onSetTimeTable: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var timeTableEdit by rememberSaveable(timeTable) { mutableStateOf(timeTable.toTimeTableString()) }

    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "设置时间表") },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = "可调整每天课程节数和时间，格式照猫画虎即可")
                Spacer(modifier = Modifier.padding(2.dp))
                OutlinedTextField(
                    value = timeTableEdit,
                    onValueChange = { timeTableEdit = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    supportingText = {
                        AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(
                onClick = { onSetTimeTable(timeTableEdit) }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// 查看所有自定义日程对话框
// 也包含编辑、删除等子对话框
@Composable
private fun CustomScheduleDialog(
    scheduleList: List<CustomScheduleEntity>,
    isGettingScheduleList: Boolean,

    onSelect: (CustomScheduleEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "自定义日程列表") },
        text = {
            if(isGettingScheduleList) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val scrollState = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    scheduleList.forEach { schedule ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp, 5.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onSelect(schedule) },
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
                                        text = schedule.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = schedule.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                ) {
                                    Text(
                                        style = MaterialTheme.typography.bodyMedium,
                                        text =
                                        if (schedule.date.year == LocalDate.now().year)
                                            schedule.date.format(DateTimeFormatter.ofPattern("MM-dd"))
                                        else
                                            schedule.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    )
                                    Text(
                                        style = MaterialTheme.typography.bodySmall,
                                        text =
                                        "${
                                            schedule.beginTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                        } ~ ${
                                            schedule.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                        }",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("返回")
            }
        },
    )
}

@Composable
internal fun CalendarSettingPage(
    onSnackBar: (String) -> Unit
) {
    val vm: CalendarViewModel = hiltViewModel()

    val currentTerm by vm.currentTermFlow.collectAsState(initial = null)
    val firstDay by vm.firstDayFlow.collectAsState(initial = null)

    val settingData by vm.settingDataFlow.collectAsState(initial = SettingData.default)

    val getFirstDayState by vm.getFirstDayStateLiveData.observeAsState()

    val getCoursesState by vm.getCoursesStateLiveData.observeAsState()

    val getExamsState by vm.getExamsStateLiveData.observeAsState()


    var showTermListDialog by rememberSaveable { mutableStateOf(false) }

    val getTermListState by vm.getTermListStateLiveData.observeAsState()

    val setCurrentTermState by vm.setCurrentTermStateLiveData.observeAsState()

    var showTimeTableDialog by rememberSaveable { mutableStateOf(false) }

    val timeTable by vm.timeTableFlow.collectAsState(initial = emptyList())

    val setTimeTableState by vm.setTimeTableStateLiveData.observeAsState()

    var showCustomSchedulesDialog by rememberSaveable { mutableStateOf(false) }


    DisposableEffect(setCurrentTermState) {
        if(setCurrentTermState is SimpleState.Success) {
            onSnackBar("设置成功")
        } else if(setCurrentTermState is SimpleState.Fail) {
            onSnackBar("设置失败")
        }
        onDispose { }
    }

    DisposableEffect(getFirstDayState) {
        if(getFirstDayState is SimpleState.Success) {
            onSnackBar("获取成功")
        } else if(getFirstDayState is SimpleState.Fail) {
            onSnackBar("获取失败")
        }
        onDispose { }
    }

    DisposableEffect(getCoursesState) {
        if(getCoursesState is SimpleState.Success) {
            onSnackBar("获取成功")
        } else if(getCoursesState is SimpleState.Fail) {
            onSnackBar("获取失败")
        }
        onDispose { }
    }

    DisposableEffect(getExamsState) {
        if(getExamsState is SimpleDataState.Success) {
            val updateCount = (getExamsState as SimpleDataState.Success).data

            onSnackBar(
                "获取成功, ${
                    if (updateCount > 0)
                        "更新了 $updateCount 条考试安排"
                    else
                        "没有更新"
                }"
            )
        } else if(getExamsState is SimpleDataState.Fail) {
            onSnackBar("获取失败")
        }
        onDispose { }
    }

    DisposableEffect(setTimeTableState) {
        if(setTimeTableState is SimpleState.Success) {
            onSnackBar("设置成功")
            showTimeTableDialog = false
        } else if(setTimeTableState is SimpleState.Fail) {
            onSnackBar("设置失败")
        }
        onDispose { }
    }

    CalendarSettingPageContent(
        currentTerm = currentTerm ?: "未设置",
        firstDay = firstDay?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "未设置",
        settingData = settingData,

        isGettingFirstDay = getFirstDayState is SimpleState.Loading || setCurrentTermState is SimpleState.Loading,
        isGettingCourses = getCoursesState is SimpleState.Loading || setCurrentTermState is SimpleState.Loading,
        isGettingExams = getExamsState is SimpleDataState.Loading || setCurrentTermState is SimpleState.Loading,

        onOpenTermListDialog = { showTermListDialog = true },
        onGetFirstDay = vm::getFirstDay,
        onGetCourses = vm::getCourses,
        onGetExams = vm::getExams,
        onOpenTimeTable = { showTimeTableDialog = true },
        onOpenCustomSchedules = { showCustomSchedulesDialog = true },
        onSettingChange = vm::setSettingData
    )

    if(showTermListDialog) {
        TermListDialog(
            term = currentTerm ?: "",
            onChangeTerm = {
                vm.setCurrentTerm(it)
                showTermListDialog = false
            },
            onDismiss = { showTermListDialog = false }
        )
    }

    if(showTimeTableDialog) {
        TimeTableDialog(
            timeTable = timeTable,
            errorMessage = if(setTimeTableState is SimpleState.Fail) "格式错误" else "",
            onSetTimeTable = vm::setTimeTable,
            onDismiss = { showTimeTableDialog = false }
        )
    }

    if(showCustomSchedulesDialog) {
        val getCustomScheduleState by vm.getCustomScheduleStateLiveData.observeAsState()
        val deleteCustomScheduleState by vm.deleteCustomScheduleStateLiveData.observeAsState()
        val editCustomScheduleState by vm.editCustomScheduleStateLiveData.observeAsState()

        val addScheduleToSysCalendarState by vm.addScheduleToSysCalendarStateLiveData.observeAsState()

        val context = LocalContext.current

        // 当前显示详情的日程, 为 null 则不显示
        var showDetail by remember { mutableStateOf<CustomScheduleEntity?>(null) }
        var editNowSchedule by remember { mutableStateOf(false) }   // 是否修改当前显示详情的日程 (这块逻辑小就不单开变量了)

        LaunchedEffect(getCustomScheduleState) {
            if(getCustomScheduleState is SimpleDataState.Fail || getCustomScheduleState == null) {
                vm.getCustomSchedules()
            }
        }

        LaunchedEffect(deleteCustomScheduleState) {
            if(deleteCustomScheduleState is SimpleState.Success) {
                onSnackBar("删除成功OvO")
                showDetail = null
                vm.getCustomSchedules()
            } else if (deleteCustomScheduleState is SimpleState.Fail) {
                onSnackBar("删除失败Orz")
            }
            vm.deleteCustomScheduleStateLiveData.value = null
        }

        LaunchedEffect(editCustomScheduleState) {
            if(editCustomScheduleState is SimpleState.Success) {
                onSnackBar("修改成功OvO")
                editNowSchedule = false
                showDetail = null
                vm.getCustomSchedules()
            } else if (editCustomScheduleState is SimpleState.Fail) {
                onSnackBar("修改失败Orz")
            }
            vm.editCustomScheduleStateLiveData.value = null
        }

        LaunchedEffect(addScheduleToSysCalendarState) {
            if(addScheduleToSysCalendarState == SimpleState.Fail) {
                onSnackBar("添加失败Orz")
            }
            vm.addScheduleToSysCalendarStateLiveData.value = null
        }

        if(editNowSchedule) {
            AddEditScheduleDialog(
                schedule = showDetail!!,
                onAddEditSchedule = {
                    vm.updateCustomSchedule(
                        scheduleEntity = showDetail!!,
                        scheduleCreateInfo = it
                    )
                },
                onDismiss = { editNowSchedule = false }
            )
        }

        if(showDetail != null) {
            CustomScheduleDetailDialog(
                schedule = showDetail!!,
                onDismiss = { showDetail = null },
                onEdit = { editNowSchedule = true },
                onDelete = vm::deleteCustomSchedule,
                onAddToCalendar = { vm.addScheduleToSysCalendar(context, it) }
            )
        }

        val scheduleList = (getCustomScheduleState as? SimpleDataState.Success)?.data ?: emptyList()

        CustomScheduleDialog(
            scheduleList = scheduleList,
            isGettingScheduleList = getCustomScheduleState is SimpleDataState.Loading,
            onSelect = { showDetail = it },
            onDismiss = { showCustomSchedulesDialog = false },
        )
    }
}