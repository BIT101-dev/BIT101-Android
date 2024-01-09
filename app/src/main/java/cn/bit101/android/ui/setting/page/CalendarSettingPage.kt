package cn.bit101.android.ui.setting.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.setting.SettingItemData
import cn.bit101.android.ui.component.setting.itemsGroup
import cn.bit101.android.ui.setting.viewmodel.CalendarViewModel
import cn.bit101.android.ui.setting.viewmodel.SettingData
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
        SettingItemData.ButtonWithSuffixText(
            title = "当前学期",
            subTitle = "设置当前学期",
            onClick = onOpenTermListDialog,
            text = currentTerm
        ),
        SettingItemData.ButtonWithSuffixText(
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
            subTitle = "设置是否显示周六",
            onClick = { onSettingChange(settingData.copy(showSaturday = it)) },
            checked = settingData.showSaturday,
        ),
        SettingItemData.Switch(
            title = "显示周日",
            subTitle = "设置是否显示周日",
            onClick = { onSettingChange(settingData.copy(showSunday = it)) },
            checked = settingData.showSunday,
        ),
        SettingItemData.Switch(
            title = "显示边框",
            subTitle = "在课程卡片上加上边框",
            onClick = { onSettingChange(settingData.copy(showBorder = it)) },
            checked = settingData.showBorder,
        ),
        SettingItemData.Switch(
            title = "高亮今日",
            subTitle = "设置今日对应的列是否高亮",
            onClick = { onSettingChange(settingData.copy(showHighlightToday = it)) },
            checked = settingData.showHighlightToday,
        ),
        SettingItemData.Switch(
            title = "显示节次分割线",
            subTitle = "用分割线将每节课分开",
            onClick = { onSettingChange(settingData.copy(showDivider = !it)) },
            checked = settingData.showDivider,
        ),
        SettingItemData.Switch(
            title = "显示当前时间线",
            subTitle = "在当前时间显示一条线",
            onClick = { onSettingChange(settingData.copy(showCurrentTime = !it)) },
            checked = settingData.showCurrentTime,
        ),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
    ) {
        itemsGroup(
            title = "数据设置",
            items = dataSettings,
        )
        itemsGroup(
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
fun TimeTableDialog(
    timeTableStr: String,
    errorMessage: String,
    onSetTimeTable: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var timeTableEdit by rememberSaveable(timeTableStr) { mutableStateOf(timeTableStr) }

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "设置时间表") },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "可调整每天课程节数和时间，格式照猫画虎即可",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = timeTableEdit,
                    onValueChange = { timeTableEdit = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    supportingText = {
                        if(errorMessage.isNotEmpty()) {
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
fun CalendarSettingPage(
    mainController: MainController,
    vm: CalendarViewModel = hiltViewModel(),
) {

    val currentTerm by vm.currentTermFlow.collectAsState(initial = null)
    val firstDay by vm.firstDayFlow.collectAsState(initial = null)

    val showDivider by vm.showDividerFlow.collectAsState(initial = false)
    val showSaturday by vm.showSaturdayFlow.collectAsState(initial = false)
    val showSunday by vm.showSundayFlow.collectAsState(initial = false)
    val showHighlightToday by vm.showHighlightTodayFlow.collectAsState(initial = false)
    val showBorder by vm.showBorderFlow.collectAsState(initial = false)
    val showCurrentTime by vm.showCurrentTimeFlow.collectAsState(initial = false)

    val settingData = SettingData(
        showDivider = showDivider,
        showSaturday = showSaturday,
        showSunday = showSunday,
        showHighlightToday = showHighlightToday,
        showBorder = showBorder,
        showCurrentTime = showCurrentTime,
    )

    val getFirstDayState by vm.getFirstDayStateLiveData.observeAsState()

    val getCoursesState by vm.getCoursesStateLiveData.observeAsState()


    var showTermListDialog by rememberSaveable { mutableStateOf(false) }

    val getTermListState by vm.getTermListStateLiveData.observeAsState()

    val setCurrentTermState by vm.setCurrentTermStateLiveData.observeAsState()

    var showTimeTableDialog by rememberSaveable { mutableStateOf(false) }

    val timeTable by vm.timeTableFlow.collectAsState(initial = "")

    val setTimeTableState by vm.setTimeTableStateLiveData.observeAsState()

    DisposableEffect(setCurrentTermState) {
        if(setCurrentTermState is SimpleState.Success) {
            mainController.snackbar("设置成功")
        } else if(setCurrentTermState is SimpleState.Fail) {
            mainController.snackbar("设置失败")
        }
        onDispose { }
    }

    DisposableEffect(getFirstDayState) {
        if(getFirstDayState is SimpleState.Success) {
            mainController.snackbar("获取成功")
        } else if(getFirstDayState is SimpleState.Fail) {
            mainController.snackbar("获取失败")
        }
        onDispose { }
    }

    DisposableEffect(getCoursesState) {
        if(getCoursesState is SimpleState.Success) {
            mainController.snackbar("获取成功")
        } else if(getCoursesState is SimpleState.Fail) {
            mainController.snackbar("获取失败")
        }
        onDispose { }
    }

    DisposableEffect(setTimeTableState) {
        if(setTimeTableState is SimpleState.Success) {
            mainController.snackbar("设置成功")
            showTimeTableDialog = false
        } else if(setTimeTableState is SimpleState.Fail) {
            mainController.snackbar("设置失败")
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
            timeTableStr = timeTable,
            errorMessage = if(setTimeTableState is SimpleState.Fail) "格式错误" else "",
            onSetTimeTable = vm::setTimeTable,
            onDismiss = { showTimeTableDialog = false }
        )
    }
}