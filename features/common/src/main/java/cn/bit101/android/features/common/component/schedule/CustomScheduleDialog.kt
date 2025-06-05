package cn.bit101.android.features.common.component.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import cn.bit101.android.features.common.utils.ScheduleCreateInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// 自定义日程详情对话框
@Composable
fun CustomScheduleDetailDialog(
    schedule: CustomScheduleEntity,
    onDismiss: () -> Unit,
    onEdit: (CustomScheduleEntity) -> Unit,
    onDelete: (CustomScheduleEntity) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog)
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = "确认删除吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(schedule)
                        showDeleteDialog = false
                    },
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("取消")
                }
            },
            text = {
                Text("日程 ${schedule.title} 删除后不可恢复")
            },
        )

    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        tonalElevation = 1.dp,
        onDismissRequest = onDismiss,
        title = {
            Text(text = "[自定义] ${schedule.title}")
        },
        text = {
            val scrollState = rememberScrollState()
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Item(title = "标题：", content = schedule.title)
                    Item("副标题：", schedule.subtitle)
                    Item("描述：", schedule.description)
                    Item("日期：", schedule.date.toString())
                    Item(
                        "时间：",
                        "${schedule.beginTime} ~ ${schedule.endTime} (${
                            schedule.beginTime.until(
                                schedule.endTime,
                                ChronoUnit.MINUTES
                            )
                        } 分钟)"
                    )

                    Spacer(modifier = Modifier.padding(2.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        AssistChip(
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            border = null,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                    alpha = 0.5f
                                )
                            ),
                            onClick = { showDeleteDialog = true },
                            label = {
                                Text(
                                    "删除",
                                    Modifier.padding(vertical = 10.dp)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete Schedule",
                                    Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        AssistChip(
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            border = null,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                    alpha = 0.5f
                                )
                            ),
                            onClick = { onEdit(schedule) },
                            label = {
                                Text(
                                    "编辑",
                                    Modifier.padding(vertical = 10.dp)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit Schedule",
                                    Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("关闭")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

// 添加 / 编辑自定义日程对话框
// schedule 为 null 时是添加, 反之是编辑
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    schedule: CustomScheduleEntity? = null,

    onAddEditSchedule: (ScheduleCreateInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    var date: LocalDateTime? by remember { mutableStateOf(schedule?.date?.atTime(LocalTime.MIN)) }
    val beginTime = remember { mutableStateOf(schedule?.beginTime) }
    val endTime = remember { mutableStateOf(schedule?.endTime) }

    // 日期选择对话框
    val showDateDialog = remember { mutableStateOf(false) }
    if (showDateDialog.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDateTime.now()).toEpochSecond(ZoneOffset.UTC) * 1000,
            initialDisplayedMonthMillis = (date ?: LocalDateTime.now()).toEpochSecond(ZoneOffset.UTC) * 1000
        )

        DatePickerDialog(
            onDismissRequest = { showDateDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 保存选择的日期
                        date = LocalDateTime.ofEpochSecond(
                            datePickerState.selectedDateMillis?.div(1000) ?: 0,
                            0,
                            ZoneOffset.UTC
                        )
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
    var timeSelected by remember { mutableStateOf(beginTime) }
    if (showTimeDialog.value) {
        val timePickerState = rememberTimePickerState(
            initialHour = (timeSelected.value ?: LocalTime.now()).hour,
            initialMinute = (timeSelected.value ?: LocalTime.now()).minute
        )

        DatePickerDialog(
            onDismissRequest = { showTimeDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 保存选择的时间
                        timeSelected.value = LocalTime.of(timePickerState.hour, timePickerState.minute)
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

    var titleEdit by rememberSaveable { mutableStateOf(schedule?.title.orEmpty()) }
    var subtitleEdit by rememberSaveable { mutableStateOf(schedule?.subtitle.orEmpty()) }
    var descriptionEdit by rememberSaveable { mutableStateOf(schedule?.description.orEmpty()) }

    val scrollState = rememberScrollState()

    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                if(schedule == null)
                    "添加自定义日程"
                else
                    "修改自定义日程"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1.0f)
                        .verticalScroll(scrollState)
                ) {
                    EditItem(
                        text = "标题",
                        value = titleEdit,
                        onValueChange = { titleEdit = it }
                    )
                    EditItem(
                        text = "副标题",
                        value = subtitleEdit,
                        onValueChange = { subtitleEdit = it }
                    )
                    EditItem(
                        text = "描述 (于详情页显示)",
                        value = descriptionEdit,
                        onValueChange = { descriptionEdit = it }
                    )

                    Spacer(modifier = Modifier.padding(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AssistChip(
                                shape = MaterialTheme.shapes.medium,
                                onClick = { showDateDialog.value = true },
                                label = {
                                    Text(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        text =
                                        when {
                                            date == null -> "日期"
                                            date!!.year == LocalDate.now().year -> date!!.format(
                                                DateTimeFormatter.ofPattern(
                                                    "MM-dd"
                                                )
                                            )

                                            else -> date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        },
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
                            AssistChip(
                                shape = MaterialTheme.shapes.medium,
                                onClick = {
                                    timeSelected = beginTime
                                    showTimeDialog.value = true
                                },
                                label = {
                                    Text(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        text =
                                        if (beginTime.value == null)
                                            "开始时间"
                                        else
                                            beginTime.value!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Schedule,
                                        contentDescription = "Select Time",
                                        Modifier.size(AssistChipDefaults.IconSize)
                                    )
                                },
                                enabled = date != null
                            )
                            AssistChip(
                                shape = MaterialTheme.shapes.medium,
                                onClick = {
                                    timeSelected = endTime
                                    showTimeDialog.value = true
                                },
                                label = {
                                    Text(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        text =
                                        if (endTime.value == null)
                                            "结束时间"
                                        else
                                            endTime.value!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Schedule,
                                        contentDescription = "Select Time",
                                        Modifier.size(AssistChipDefaults.IconSize)
                                    )
                                },
                                enabled = date != null && beginTime.value != null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(5.dp))

                Text(
                    text = "请不要把时间设定在课间, 会无法显示, 过早或过晚的时间也会被截断, 持续时间过短的日程也不会显示\n" +
                            "此外, 时间冲突的日程会相互覆盖, 所以尽量不要和其它日程冲突, 可能会错过上课甚至考试"
                )
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(
                onClick = {
                    onAddEditSchedule(
                        ScheduleCreateInfo(
                            title = titleEdit,
                            subtitle = subtitleEdit,
                            description = descriptionEdit,
                            date = date!!.toLocalDate(),
                            beginTime = beginTime.value!!,
                            endTime = endTime.value!!,
                        )
                    )
                },
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
internal fun EditItem(
    text: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Text(text)
    Spacer(modifier = Modifier.padding(2.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    )
}