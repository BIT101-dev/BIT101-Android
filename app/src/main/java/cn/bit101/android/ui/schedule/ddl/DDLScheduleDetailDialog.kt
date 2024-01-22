package cn.bit101.android.ui.schedule.ddl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.ui.MainController
import java.time.format.DateTimeFormatter

/**
 * @author flwfdd
 * @date 13/05/2023 21:13
 * @description 日程详情对话框
 * _(:з」∠)_
 */

@Composable
fun DDLScheduleDetailDialog(
    mainController: MainController,
    vm: DDLScheduleViewModel,
    event: DDLScheduleEntity,
    showDialog: MutableState<Boolean>,
    showEditDialog: (item: DDLScheduleEntity) -> Unit
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    if (showDeleteDialog.value)
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(text = "确认删除吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteDDL(event)
                        showDeleteDialog.value = false
                        showDialog.value = false
                        mainController.snackbar("删除成功OvO")
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("取消")
                }
            },
            text = {
                Text("${event.title} 删除后不可恢复")
            },
        )

    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        tonalElevation = 1.dp,
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Text(text = event.title)
        },
        text = {
            val scrollState = rememberScrollState()
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Item(
                        title = "DDL：",
                        content = event.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                    Item(
                        title = "分组：",
                        content = (if (event.group == "lexue") "乐学" else "自定义")
                    )
                    Item(title = "详情：", content = event.text)

                    if (event.group != "lexue")
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
                                onClick = { showDeleteDialog.value = true },
                                label = {
                                    Text(
                                        "删除",
                                        Modifier.padding(vertical = 10.dp)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete DDL",
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
                                onClick = {
                                    showDialog.value = false
                                    showEditDialog(event)
                                },
                                label = {
                                    Text(
                                        "编辑",
                                        Modifier.padding(vertical = 10.dp)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit DDL",
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
                onClick = {
                    showDialog.value = false
                }
            ) {
                Text("关闭")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Composable
private fun Item(title: String, content: String) {
    Spacer(modifier = Modifier.height(10.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
