package cn.bit101.android.ui.schedule.ddl

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.database.entity.DDLScheduleEntity
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 13/05/2023 15:57
 * @description DDL主页面
 * _(:з」∠)_
 */


@Composable
fun DDLSchedule(
    mainController: MainController,
    active: Boolean,
    vm: DDLScheduleViewModel = hiltViewModel()
) {
    // 编辑日程弹窗
    val showEditDialog = remember { mutableStateOf(false) }
    var editData: DDLScheduleEntity? by remember { mutableStateOf(null) }
    if (showEditDialog.value) {
        DDLScheduleEditDialog(mainController, vm, item = editData, showDialog = showEditDialog)
    }

    // 日程详情弹窗
    val showDetailDialog = remember { mutableStateOf(false) }
    var detailData: DDLScheduleEntity? by remember { mutableStateOf(null) }
    if (showDetailDialog.value && detailData != null) {
        DDLScheduleDetailDialog(
            mainController = mainController,
            vm = vm,
            event = detailData!!,
            showDialog = showDetailDialog,
            showEditDialog = {
                editData = it
                showEditDialog.value = true
            })
    }

    // 判断是否已经有订阅链接
    val url = vm.lexueCalendarUrlFlow.collectAsState(initial = null)
    if (url.value.isNullOrBlank()) {
        // 还没有订阅链接
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var loading by remember { mutableStateOf(false) }
            Button(enabled = !loading, onClick = {
                MainScope().launch {
                    loading = true
                    vm.updateLexueCalendarUrl()
                    vm.updateLexueCalendar()
                    loading = false
                }
            }) {
                if (loading) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                else Text("获取乐学日程")
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            // 日程列表
            val events = vm.events.collectAsState()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (events.value.isEmpty()) {
                    item {
                        Text(
                            text = "怎么会有人没事儿了啊ヽ(`Д´)ﾉ\n快去卷😭",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp, 5.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(10.dp, 5.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                } else {
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
                }

                // 防止悬浮按钮遮挡
                item {
                    Spacer(modifier = Modifier.height(124.dp))
                }
            }

            var showConfigDialog by rememberSaveable { mutableStateOf(false) }

            // 悬浮按钮组
            val fabSize = 42.dp
            Column(
                modifier = Modifier
                    .padding(10.dp, 20.dp)
            ) {
                // 添加按钮
                FloatingActionButton(
                    modifier = Modifier
                        .size(fabSize),
                    onClick = {
                        editData = null
                        showEditDialog.value = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "next week",
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // 设置按钮
                FloatingActionButton(
                    modifier = Modifier
                        .size(fabSize),
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
}


// 日程设置对话框
@Composable
fun DDLScheduleConfigDialog(
    mainController: MainController,
    vm: DDLScheduleViewModel,
    onDismiss: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp, 10.dp, 0.dp),
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
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                items = listOf(
                    ConfigItem.Button(
                        title = "刷新",
                        content = if (refreshing) "刷新中..." else "点击拉取乐学日程",
                        onClick = {
                            MainScope().launch {
                                if (!refreshing) {
                                    refreshing = true
                                    if (vm.updateLexueCalendar()) mainController.snackbar("刷新成功OvO")
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
                    ConfigItem.Button(
                        title = "获取订阅链接",
                        content = if (refreshing) "获取中..." else "重新获取日程订阅链接",
                        onClick = {
                            MainScope().launch {
                                if (!refreshing) {
                                    refreshing = true
                                    if (vm.updateLexueCalendarUrl()) mainController.snackbar("获取成功OvO")
                                    else mainController.snackbar("获取失败Orz")
                                    refreshing = false
                                }
                            }
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
