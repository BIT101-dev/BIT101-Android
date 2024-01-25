package cn.bit101.android.features.setting.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.config.setting.base.TimeTable
import cn.bit101.android.config.setting.base.toTimeTableString
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.CalendarViewModel
import cn.bit101.android.features.setting.viewmodel.SettingData
import java.time.format.DateTimeFormatter


@Composable
private fun CalendarSettingPageContent(
    currentTerm: String,
    firstDay: String,
    settingData: SettingData,

    isGettingFirstDay: Boolean,
    isGettingCourses: Boolean,

    onOpenTermListDialog: () -> Unit,
    onGetFirstDay: () -> Unit,
    onGetCourses: () -> Unit,
    onOpenTimeTable: () -> Unit,
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
            title = "时间表",
            subTitle = "每节课的上课和下课时间",
            onClick = onOpenTimeTable,
        )

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
    )

    SettingsColumn {
        SettingsGroup(
            title = "数据设置",
            subTitle = "如果获取失败可以尝试在账号设置中“检查登录状态”哦",
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
    termList: List<String>,
    isGettingTermList: Boolean,

    onChangeTerm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedOption = if(termList.contains(term)) term
    else termList.firstOrNull() ?: ""

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "切换学期") },
        text = {
            if(isGettingTermList || termList.isEmpty()) {
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
                        .selectableGroup()
                        .verticalScroll(scrollState)
                ) {
                    termList.forEach { text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = {
                                        onChangeTerm(text)
                                        onDismiss()
                                    },
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
        confirmButton = {},
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


    var showTermListDialog by rememberSaveable { mutableStateOf(false) }

    val getTermListState by vm.getTermListStateLiveData.observeAsState()

    val setCurrentTermState by vm.setCurrentTermStateLiveData.observeAsState()

    var showTimeTableDialog by rememberSaveable { mutableStateOf(false) }

    val timeTable by vm.timeTableFlow.collectAsState(initial = emptyList())

    val setTimeTableState by vm.setTimeTableStateLiveData.observeAsState()

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

        onOpenTermListDialog = { showTermListDialog = true },
        onGetFirstDay = vm::getFirstDay,
        onGetCourses = vm::getCourses,
        onOpenTimeTable = { showTimeTableDialog = true },
        onSettingChange = vm::setSettingData
    )

    if(showTermListDialog) {
        LaunchedEffect(getTermListState) {
            if(getTermListState is SimpleDataState.Fail || getTermListState == null) {
                vm.getTermList()
            }
        }

        val termList = (getTermListState as? SimpleDataState.Success)?.data ?: emptyList()

        TermListDialog(
            term = currentTerm ?: "",
            termList = termList,
            isGettingTermList = getTermListState is SimpleDataState.Loading,
            onChangeTerm = vm::setCurrentTerm,
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
}