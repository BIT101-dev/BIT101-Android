package cn.bit101.android.ui.schedule

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
import cn.bit101.android.MainController
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.viewmodel.ScheduleViewModel
import cn.bit101.android.viewmodel.checkTimeTable
import cn.bit101.android.viewmodel.getCoursesFromNet
import cn.bit101.android.viewmodel.getTermsFromNet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 15/05/2023 00:35
 * @description _(:з」∠)_
 */

// 课表设置对话框
@Composable
fun CourseScheduleConfigDialog(
    mainController: MainController,
    vm: ScheduleViewModel,
    onDismiss: () -> Unit
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
        val term = vm.termFlow.collectAsState(initial = null).value
        var refreshing by remember { mutableStateOf(false) }
        ConfigColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp),
            items = listOf(
                ConfigItem.Button(
                    title = "切换学期",
                    content = term ?: "未选择",
                    onClick = {
                        showTermListDialog.value = true
                    }
                ),
                ConfigItem.Button(
                    title = "刷新课表",
                    content = if (refreshing) "刷新中..." else "点击重新拉取课表",
                    onClick = {
                        MainScope().launch {
                            if (term == null) {
                                mainController.snackbar("未选择学期")
                                return@launch
                            }
                            if (!refreshing) {
                                refreshing = true
                                if (getCoursesFromNet(term)) mainController.snackbar("刷新成功OvO")
                                else mainController.snackbar("刷新失败Orz")
                                refreshing = false
                            }
                        }
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
                ConfigItem.Switch(
                    title = "显示当前时间线",
                    checked = vm.showCurrentTime.collectAsState(initial = true).value,
                    onCheckedChange = {
                        vm.setShowCurrentTime(it)
                    }
                ),
                ConfigItem.Button(
                    title = "设置时间表",
                    content = "点击设置节次及时间",
                    onClick = {
                        showTimeTableDialog.value = true
                    }
                ),
            ))

        if (showTermListDialog.value) {
            TermListDialog(mainController, vm, showTermListDialog)
        }

        if (showTimeTableDialog.value) {
            TimeTableDialog(mainController, vm, showTimeTableDialog)
        }

    }
}

// 选择学期对话框
@Composable
fun TermListDialog(
    mainController: MainController,
    vm: ScheduleViewModel,
    showDialog: MutableState<Boolean>
) {
    var termList by remember { mutableStateOf(listOf("")) }
    val (selectedOption, onOptionSelected) = rememberSaveable { mutableStateOf("") }
    // 默认选择第一项
    LaunchedEffect(showDialog) {
        termList = getTermsFromNet()
        if (termList.isNotEmpty()) onOptionSelected(termList[0])
    }
    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = {
            showDialog.value = false
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
                            mainController.snackbar("切换失败Orz")
                        }
                    )
                    showDialog.value = false
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                }
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
    vm: ScheduleViewModel,
    showDialog: MutableState<Boolean>
) {
    var timeTableEdit by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf("") }
    LaunchedEffect(showDialog) {
        vm.timeTableStringFlow.first().let {
            timeTableEdit = TextFieldValue(it)
        }
    }
    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = {
            showDialog.value = false
        },
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
                onClick = {
                    if (checkTimeTable(timeTableEdit.text)) {
                        vm.setTimeTable(timeTableEdit.text)
                        mainController.snackbar("设置成功OvO")
                        showDialog.value = false
                    } else {
                        errorMessage = "格式校验失败Orz"
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                }
            ) {
                Text("取消")
            }
        }
    )
}
