package cn.bit101.android.ui.schedule.ddl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.ui.MainController
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
                    onClick = { mainController.navController.navigate("setting?route=ddl") },
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
        }

    }
}