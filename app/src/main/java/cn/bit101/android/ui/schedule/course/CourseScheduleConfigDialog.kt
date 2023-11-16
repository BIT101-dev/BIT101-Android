package cn.bit101.android.ui.schedule.course

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    showDivider: Boolean,
    showSaturday: Boolean,
    showSunday: Boolean,
    showHighlightToday: Boolean,
    showBorder: Boolean,
    timeTable: String,
    currentTime: Boolean,

    coursesRefreshing: Boolean,

    changeTermState: SimpleState?,
    setTimeTableState: SimpleState?,
    getTermListState: SimpleDataState<List<String>>?,

    onUpdateCourses: () -> Unit,
    onSetShowDivider: (Boolean) -> Unit,
    onSetShowSaturday: (Boolean) -> Unit,
    onSetShowSunday: (Boolean) -> Unit,
    onSetShowHighlightToday: (Boolean) -> Unit,
    onSetShowBorder: (Boolean) -> Unit,
    onSetCurrentTime: (Boolean) -> Unit,

    onRefreshTermList: () -> Unit,
    onChangeTerm: (String) -> Unit,
    onSetTimeTable: (String) -> Unit,

    onClearChangeTermState: () -> Unit,

    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

        val showTermListDialog = rememberSaveable { mutableStateOf(false) }
        val showTimeTableDialog = rememberSaveable { mutableStateOf(false) }

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
                    onClick = onUpdateCourses
                ),
                ConfigItem.Switch(
                    title = "显示周六",
                    checked = showSaturday,
                    onCheckedChange = onSetShowSaturday
                ),
                ConfigItem.Switch(
                    title = "显示周日",
                    checked = showSunday,
                    onCheckedChange = onSetShowSunday
                ),
                ConfigItem.Switch(
                    title = "显示边框",
                    checked = showBorder,
                    onCheckedChange = onSetShowBorder
                ),
                ConfigItem.Switch(
                    title = "高亮今日",
                    checked = showHighlightToday,
                    onCheckedChange = onSetShowHighlightToday
                ),
                ConfigItem.Switch(
                    title = "显示节次分割线",
                    checked = showDivider,
                    onCheckedChange = onSetShowDivider
                ),
                ConfigItem.Switch(
                    title = "显示当前时间线",
                    checked = currentTime,
                    onCheckedChange = onSetCurrentTime
                ),
                ConfigItem.Button(
                    title = "设置时间表",
                    content = "点击设置节次及时间",
                    onClick = { showTimeTableDialog.value = true }
                ),
            ))

        if (showTermListDialog.value) {
            TermListDialog(
                mainController = mainController,
                term = term,
                getTermListState = getTermListState,
                changeTermState = changeTermState,
                onRefreshTermList = onRefreshTermList,
                onChangeTerm = onChangeTerm,
                onClearChangeTermState = onClearChangeTermState,
                onDismiss = { showTermListDialog.value = false }
            )
        }

        if (showTimeTableDialog.value) {
            TimeTableDialog(
                mainController = mainController,
                timeTable = timeTable,
                setTimeTableState = setTimeTableState,

                onSetTimeTable = onSetTimeTable,
                onDismiss = { showTimeTableDialog.value = false },
            )
        }
    }
}

// 选择学期对话框
@Composable
fun TermListDialog(
    mainController: MainController,
    term: String,
    getTermListState: SimpleDataState<List<String>>?,

    changeTermState: SimpleState?,

    onClearChangeTermState: () -> Unit,

    onRefreshTermList: () -> Unit,
    onChangeTerm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedOption by rememberSaveable { mutableStateOf("") }

    // 更改学期成功后自动关闭对话框，并显示提示
    DisposableEffect(changeTermState) {
        if(changeTermState == SimpleState.Success) {
            onDismiss()
            mainController.scope.launch {
                mainController.snackbarHostState.showSnackbar("切换成功Orz")
            }
        } else if(changeTermState == SimpleState.Error) {
            mainController.scope.launch {
                mainController.snackbarHostState.showSnackbar("切换失败Orz")
            }
        }
        onDispose {
            // 重置状态
            selectedOption = ""
            onClearChangeTermState()
        }
    }


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
                onClick = { onChangeTerm(selectedOption) }
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
    mainController: MainController,

    timeTable: String,
    setTimeTableState: SimpleState?,

    onSetTimeTable: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var timeTableEdit by remember(timeTable) { mutableStateOf(TextFieldValue(timeTable)) }
    var errorMessage by remember { mutableStateOf("") }

    DisposableEffect(setTimeTableState) {
        if(setTimeTableState == SimpleState.Success) {
            onDismiss()
            mainController.scope.launch {
                mainController.snackbarHostState.showSnackbar("设置成功OvO")
            }
            errorMessage = ""
        } else if(setTimeTableState == SimpleState.Error) {
            mainController.scope.launch {
                mainController.snackbarHostState.showSnackbar("格式校验失败Orz")
            }
            errorMessage = "格式校验失败Orz"
        }
        onDispose {}
    }

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
