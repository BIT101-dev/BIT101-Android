package cn.bit101.android.features.schedule.ddl

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
import androidx.compose.runtime.LaunchedEffect
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import androidx.compose.material3.TextButton
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
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.nav.NavDest

/**
 * @author flwfdd
 * @date 13/05/2023 15:57
 * @description DDL主页面
 * _(:з」∠)_
 */


@Composable
internal fun DDLSchedule(
    mainController: MainController,
    active: Boolean,
    vm: DDLScheduleViewModel = hiltViewModel()
) {
    val events by vm.events.collectAsState()
    val refreshState by vm.refreshState.collectAsState()
    val editState by vm.editState.collectAsState()
    val loadError by vm.loadError.collectAsState()
    LaunchedEffect(refreshState) {
        (refreshState as? SimpleErrorDataState.Fail)?.let { mainController.snackbar(it.msg) }
    }
    LaunchedEffect(editState) {
        when (val state = editState) {
            is SimpleErrorDataState.Success -> mainController.snackbar(state.data)
            is SimpleErrorDataState.Fail -> mainController.snackbar(state.msg)
            else -> Unit
        }
    }
    // 编辑日程弹窗
    val showEditDialog = remember { mutableStateOf(false) }
    var editData: DDLScheduleEntity? by remember { mutableStateOf(null) }
    if (showEditDialog.value) {
        DDLScheduleEditDialog(vm, item = editData, showDialog = showEditDialog)
    }

    // 日程详情弹窗
    val showDetailDialog = remember { mutableStateOf(false) }
    var detailData: DDLScheduleEntity? by remember { mutableStateOf(null) }
    val currentDetail = detailData?.let { selected ->
        events.firstOrNull { it.id == selected.id && it.ownerSid == selected.ownerSid }
    }
    LaunchedEffect(currentDetail) {
        if (currentDetail == null) {
            showDetailDialog.value = false
            detailData = null
        }
    }
    if (showDetailDialog.value && currentDetail != null) {
        DDLScheduleDetailDialog(
            vm = vm,
            event = currentDetail,
            showDialog = showDetailDialog,
            showEditDialog = {
                editData = it
                showEditDialog.value = true
            })
    }

    // 判断是否已经有订阅链接
    val url = vm.lexueCalendarUrlFlow.collectAsState(initial = null)
    if (url.value.isNullOrBlank() && events.isEmpty()) {
        // 没有本地日程时显示订阅入口，已有自定义或当前账号日程仍可离线查看
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loadError != null) {
                TextButton(onClick = { vm.loadEvents() }) { Text(loadError.orEmpty()) }
            }
            if (url.value == null && loadError == null) CircularProgressIndicator()
            val loading = refreshState is SimpleErrorDataState.Loading || url.value == null
            Button(enabled = !loading, onClick = { vm.refreshCalendar(acquireUrl = true) }) {
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (loadError != null) {
                    item {
                        TextButton(onClick = { vm.loadEvents() }, modifier = Modifier.fillMaxWidth()) {
                            Text(loadError.orEmpty())
                        }
                    }
                }
                if (events.isEmpty()) {
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
                    itemsIndexed(events) { _, item ->
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
                    onClick = { mainController.navigate(NavDest.Setting("ddl")) },
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