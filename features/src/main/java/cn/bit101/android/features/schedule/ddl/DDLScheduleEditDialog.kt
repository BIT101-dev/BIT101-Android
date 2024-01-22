package cn.bit101.android.features.schedule.ddl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.features.MainController
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * @author flwfdd
 * @date 2023/8/18 下午2:42
 * @description 编辑或添加DDL对话框
 * _(:з」∠)_
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDLScheduleEditDialog(
    mainController: MainController,
    vm: DDLScheduleViewModel,
    item: DDLScheduleEntity?,
    showDialog: MutableState<Boolean>
) {
    var title by remember { mutableStateOf(TextFieldValue(item?.title ?: "")) }
    var dateTime: LocalDateTime? by remember { mutableStateOf(item?.time) }
    var text by remember { mutableStateOf(TextFieldValue(item?.text ?: "")) }

    // 日期选择对话框
    val showDateDialog = remember { mutableStateOf(false) }
    if (showDateDialog.value) {
        val datePickerState = if (dateTime == null) rememberDatePickerState(
            initialSelectedDateMillis = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000,
            initialDisplayedMonthMillis = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000
        ) else
            rememberDatePickerState(
                initialSelectedDateMillis = dateTime!!.toEpochSecond(ZoneOffset.UTC) * 1000,
                initialDisplayedMonthMillis = dateTime!!.toEpochSecond(ZoneOffset.UTC) * 1000
            )

        DatePickerDialog(
            onDismissRequest = { showDateDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 保存选择的日期
                        dateTime = LocalDateTime.ofEpochSecond(
                            datePickerState.selectedDateMillis?.div(1000) ?: 0,
                            0,
                            ZoneOffset.UTC
                        ).withHour(dateTime?.hour ?: 0).withMinute(dateTime?.minute ?: 0)
                        showDateDialog.value = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDateDialog.value = false
                    }
                ) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 时间选择对话框
    val showTimeDialog = remember { mutableStateOf(false) }
    if (showTimeDialog.value) {
        val timePickerState = if (dateTime == null) rememberTimePickerState() else
            rememberTimePickerState(
                initialHour = dateTime!!.hour,
                initialMinute = dateTime!!.minute
            )

        DatePickerDialog(
            onDismissRequest = { showTimeDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 保存选择的时间
                        dateTime = dateTime!!.withHour(timePickerState.hour)
                            .withMinute(timePickerState.minute).withSecond(0)
                        showTimeDialog.value = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimeDialog.value = false
                    }
                ) {
                    Text("取消")
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState, modifier = Modifier.padding(24.dp))
            }

        }
    }

    val scrollState = rememberScrollState()
    var alertMsg by remember { mutableStateOf("") }
    LaunchedEffect(alertMsg) {
        if (alertMsg.isNotEmpty()) {
            scrollState.animateScrollTo(0)
        }
    }
    AlertDialog(
        modifier = Modifier
            .fillMaxHeight(0.75f)
            .fillMaxWidth(0.9f),
        tonalElevation = 1.dp,
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Text(text = if (item == null) "添加DDL" else "编辑DDL")
        },
        text = {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // 提示文本
                    if (alertMsg.isNotEmpty()) Text(
                        text = alertMsg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        AssistChip(
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            onClick = { showDateDialog.value = true },
                            label = {
                                Text(
                                    if (dateTime == null) "请选择日期" else dateTime!!.format(
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                    ),
                                    Modifier.padding(vertical = 10.dp)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Today,
                                    contentDescription = "Select Date",
                                    Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        AssistChip(
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            onClick = { showTimeDialog.value = true },
                            label = {
                                Text(
                                    if (dateTime == null) "请选择时间" else dateTime!!.format(
                                        DateTimeFormatter.ofPattern("HH:mm:ss")
                                    ),
                                    Modifier.padding(vertical = 10.dp)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = "Select Time",
                                    Modifier.size(AssistChipDefaults.IconSize)
                                )
                            },
                            enabled = dateTime != null
                        )
                    }

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("详情") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.text.isEmpty()) {
                        alertMsg = "标题不能为空"

                        return@TextButton
                    }
                    if (dateTime == null) {
                        alertMsg = "请选择日期和时间"
                        return@TextButton
                    }
                    if (item == null) {
                        vm.addDDL(title.text, dateTime!!, text.text)
                        mainController.snackbar("添加成功OvO")
                    } else {
                        vm.updateDDL(item, title.text, dateTime!!, text.text)
                        mainController.snackbar("修改成功OvO")
                    }
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
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}