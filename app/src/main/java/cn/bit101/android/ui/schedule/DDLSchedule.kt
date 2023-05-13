package cn.bit101.android.ui.schedule

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.MainController
import cn.bit101.android.database.DDLScheduleEntity
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.viewmodel.DDLScheduleViewModel
import cn.bit101.android.viewmodel.updateLexueCalendar
import cn.bit101.android.viewmodel.updateLexueCalendarUrl
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 13/05/2023 15:57
 * @description _(:з」∠)_
 */


@Composable
fun DDLSchedule(
    mainController: MainController,
    active: Boolean,
    vm: DDLScheduleViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {

        // 日程详情弹窗
        val showDetailDialog = remember { mutableStateOf(false) }
        var detailData: DDLScheduleEntity? by remember { mutableStateOf(null) }
        if (showDetailDialog.value && detailData != null) {
            DDLDetailDialog(event = detailData!!, showDialog = showDetailDialog)
        }

        // 判断是否已经有订阅链接
        val url = vm.lexueCalendarUrlFlow.collectAsState(initial = null)
        if (url.value == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    MainScope().launch {
                        updateLexueCalendarUrl()
                    }
                }) {
                    Text("获取乐学日历")
                }
            }
        } else {
            val events = vm.events.collectAsState()

            LazyColumn {
                itemsIndexed(events.value) { _, item ->
                    DDLScheduleItem(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp, 5.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                detailData = item
                                showDetailDialog.value = true
                            }, item, vm
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        var showConfigDialog by rememberSaveable { mutableStateOf(false) }

        FloatingActionButton(
            modifier = Modifier
                .padding(10.dp, 20.dp)
                .size(42.dp),
            onClick = {
                showConfigDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
            contentColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(0.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "settings",
            )
        }

        // 设置对话框 自定义进入和退出动画
        AnimatedVisibility(
            visible = showConfigDialog,
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
            DDLScheduleConfigDialog(mainController, vm) {
                showConfigDialog = false
            }
        }

        // 响应返回键 收起设置对话框
        BackHandler(enabled = showConfigDialog && active) {
            showConfigDialog = false
        }
    }
}

fun mixColor(color1: Color, color2: Color, ratio: Float): Color {
    return Color(
        (color1.red * ratio + color2.red * (1 - ratio)),
        (color1.green * ratio + color2.green * (1 - ratio)),
        (color1.blue * ratio + color2.blue * (1 - ratio))
    )
}

@Composable
fun DDLScheduleItem(modifier: Modifier, item: DDLScheduleEntity, vm: DDLScheduleViewModel) {
    Surface(
        modifier = modifier,
        color = if (item.done) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else mixColor(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.errorContainer,
            vm.remainTimeRatio(item.time)
        ),
        contentColor = if (item.done) MaterialTheme.colorScheme.onSecondaryContainer else mixColor(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.onErrorContainer,
            vm.remainTimeRatio(item.time)
        ),
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
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = vm.remainTime(item.time),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Checkbox(checked = item.done, onCheckedChange = {
                vm.setDone(item, it)
            })
        }
    }
}


// 日程设置对话框
@Composable
fun DDLScheduleConfigDialog(
    mainController: MainController,
    vm: DDLScheduleViewModel,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp, 10.dp, 10.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "DDL设置", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { onDismiss() }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "close config dialog",
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val showInputNumberDialog = remember { mutableStateOf(false) }
        val inputNumberDialogTitle = remember { mutableStateOf("") }
        val inputNumberDialogText = remember { mutableStateOf("") }
        val inputNumberDialogValue = remember { mutableStateOf(0) }
        val inputNumberDialogOnChange = remember { mutableStateOf({ _: Int -> }) }
        var refreshing by remember { mutableStateOf(false) }
        ConfigColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            items = listOf(
                ConfigItem.Button(
                    title = "刷新",
                    content = if (refreshing) "刷新中..." else "点击拉取乐学日程",
                    onClick = {
                        MainScope().launch {
                            if (!refreshing) {
                                refreshing = true
                                if (updateLexueCalendar()) mainController.snackbar("刷新成功OvO")
                                else mainController.snackbar("刷新失败Orz")
                                refreshing = false
                            }
                        }
                    }
                ),
                ConfigItem.Button(
                    title = "设置变色天数",
                    content = "临近日程会改变颜色",
                    onClick = {
                        inputNumberDialogTitle.value = "设置变色天数"
                        inputNumberDialogText.value = "距离DDL多少天时开始改变颜色"
                        inputNumberDialogValue.value = vm.beforeDay
                        inputNumberDialogOnChange.value = {
                            vm.setBeforeDay(it.toLong())
                        }
                        showInputNumberDialog.value = true
                    }
                ),
                ConfigItem.Button(
                    title = "设置滞留天数",
                    content = "过期日程会继续显示",
                    onClick = {
                        inputNumberDialogTitle.value = "设置滞留天数"
                        inputNumberDialogText.value = "过期日程继续显示多少天"
                        inputNumberDialogValue.value = vm.afterDay
                        inputNumberDialogOnChange.value = {
                            vm.setAfterDay(it.toLong())
                        }
                        showInputNumberDialog.value = true
                    }
                ),
            ))

        if (showInputNumberDialog.value) InputNumberDialog(
            mainController = mainController,
            title = inputNumberDialogTitle.value,
            text = inputNumberDialogText.value,
            initValue = inputNumberDialogValue.value,
            onChange = inputNumberDialogOnChange.value,
            showDialog = showInputNumberDialog
        )
    }
}

// 输入数字对话框
@Composable
fun InputNumberDialog(
    mainController: MainController,
    title: String,
    text: String,
    initValue: Int,
    onChange: (Int) -> Unit,
    showDialog: MutableState<Boolean>
) {
    var editValue by remember { mutableStateOf(TextFieldValue(initValue.toString())) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Text(text = title)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    if (editValue.text.toIntOrNull() != null && editValue.text.toInt() >= 0) {
                        onChange(editValue.text.toInt())
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
