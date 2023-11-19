package cn.bit101.android.ui.schedule.course

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 15/05/2023 00:35
 * @description 课表设置对话框
 * _(:з」∠)_
 */

@Composable
fun CourseScheduleConfigDialog(
    mainController: MainController,
    term: String,
    settingData: SettingData,
    timeTableStr: String,

    coursesRefreshing: Boolean,

    setTimeTableState: SimpleState?,
    changeTermState: SimpleState?,

    onClearStates: () -> Unit,

    getTermListState: SimpleDataState<List<String>>?,

    onSetSetting: (SettingData) -> Unit,
    onSetTimeTableStr: (String) -> Unit,

    onForceRefreshCourses: () -> Unit,
    onRefreshTermList: () -> Unit,
    onChangeTerm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var setTimeTableError by remember { mutableStateOf("") }

    val showTermListDialog = rememberSaveable { mutableStateOf(false) }
    val showTimeTableDialog = rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { onClearStates() }
    }

    DisposableEffect(setTimeTableState) {
        if(setTimeTableState == SimpleState.Success) {
            setTimeTableError = ""
            showTimeTableDialog.value = false
            mainController.snackbar("设置成功OvO")
        } else if(setTimeTableState == SimpleState.Error) {
            setTimeTableError = "格式校验失败Orz"
        }
        onDispose {}
    }

    // 切换学期的状态改变
    DisposableEffect(changeTermState) {
        if(changeTermState == SimpleState.Success) {
            mainController.snackbar("切换成功OvO")
        } else if(changeTermState == SimpleState.Error) {
            mainController.snackbar("切换失败Orz")
        }
        onDispose {}
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp, 10.dp, 0.dp),
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

            ConfigColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                items = listOf(
                    ConfigItem.Button(
                        title = "切换学期",
                        content = term,
                        onClick = {
                            showTermListDialog.value = true
                        }
                    ),
                    ConfigItem.Button(
                        title = "刷新课表",
                        content = if (coursesRefreshing) "刷新中..." else "点击重新拉取课表",
                        onClick = onForceRefreshCourses
                    ),
                    ConfigItem.Switch(
                        title = "显示周六",
                        checked = settingData.showSaturday,
                        onCheckedChange = { onSetSetting(settingData.copy(showSaturday = it)) }
                    ),
                    ConfigItem.Switch(
                        title = "显示周日",
                        checked = settingData.showSunday,
                        onCheckedChange = { onSetSetting(settingData.copy(showSunday = it)) }
                    ),
                    ConfigItem.Switch(
                        title = "显示边框",
                        checked = settingData.showBorder,
                        onCheckedChange = { onSetSetting(settingData.copy(showBorder = it)) }
                    ),
                    ConfigItem.Switch(
                        title = "高亮今日",
                        checked = settingData.showHighlightToday,
                        onCheckedChange = { onSetSetting(settingData.copy(showHighlightToday = it)) }
                    ),
                    ConfigItem.Switch(
                        title = "显示节次分割线",
                        checked = settingData.showDivider,
                        onCheckedChange = { onSetSetting(settingData.copy(showDivider = it)) }
                    ),
                    ConfigItem.Switch(
                        title = "显示当前时间线",
                        checked = settingData.showCurrentTime,
                        onCheckedChange = { onSetSetting(settingData.copy(showCurrentTime = it)) }
                    ),
                    ConfigItem.Button(
                        title = "设置时间表",
                        content = "点击设置节次及时间",
                        onClick = { showTimeTableDialog.value = true }
                    ),
                ))

            if (showTermListDialog.value) {
                TermListDialog(
                    term = term,
                    getTermListState = getTermListState,

                    onRefreshTermList = onRefreshTermList,
                    onChangeTerm = onChangeTerm,
                    onDismiss = { showTermListDialog.value = false }
                )
            }

            if (showTimeTableDialog.value) {
                TimeTableDialog(
                    timeTableStr = timeTableStr,
                    errorMessage = setTimeTableError,

                    onSetTimeTable = onSetTimeTableStr,
                    onDismiss = { showTimeTableDialog.value = false },
                )
            }
        }
    }
}

// 选择学期对话框
@Composable
fun TermListDialog(
    term: String,
    getTermListState: SimpleDataState<List<String>>?,

    onRefreshTermList: () -> Unit,
    onChangeTerm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedOption by rememberSaveable { mutableStateOf("") }
    // 先刷新学期列表，再选择第一项
    LaunchedEffect(getTermListState) {
        if(getTermListState == null) {
            onRefreshTermList()
        } else if(getTermListState is SimpleDataState.Success) {
            val index = getTermListState.data.indexOf(term)
            selectedOption = if(index == -1) getTermListState.data[0]
            else term
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onDismiss,
        title = {
            Text(text = "切换学期")
        },
        text = {

            when(getTermListState) {
                null, is SimpleDataState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SimpleDataState.Success -> {
                    val termList = getTermListState.data
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
                                        onClick = { selectedOption = text },
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
                is SimpleDataState.Error -> {

                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onChangeTerm(selectedOption)
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        }
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
    var timeTableEdit by remember(timeTableStr) { mutableStateOf(TextFieldValue(timeTableStr)) }

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onDismiss,
        title = {
            Text(text = "设置时间表")
        },
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
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSetTimeTable(timeTableEdit.text) }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        }
    )
}
