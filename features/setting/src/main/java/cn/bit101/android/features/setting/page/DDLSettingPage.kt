package cn.bit101.android.features.setting.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.DDLViewModel


@Composable
private fun DDLSettingPageContent(
    afterDay: String,
    beforeDay: String,

    isUpdatingLexueCalendarUrl: Boolean,
    isUpdatingLexueCalendar: Boolean,

    onUpdateLexueCalendarUrl: () -> Unit,
    onUpdateLexueCalendar: () -> Unit,

    onOpenAfterDayDialog: () -> Unit,
    onOpenBeforeDayDialog: () -> Unit,
) {
    val dataItems = listOf(
        SettingItemData.Button(
            title = "重新获取订阅链接",
            subTitle = "重新获取订阅链接",
            onClick = onUpdateLexueCalendarUrl,
            enable = !isUpdatingLexueCalendarUrl,
        ),
        SettingItemData.Button(
            title = "重新拉取乐学日程",
            subTitle = "请先获取订阅链接哦",
            onClick = onUpdateLexueCalendar,
            enable = !isUpdatingLexueCalendar,
        ),
    )

    val displayItems = listOf(
        SettingItemData.Button(
            title = "变色天数",
            subTitle = "临近日程会改变颜色",
            onClick = onOpenBeforeDayDialog,
            text = beforeDay,
        ),
        SettingItemData.Button(
            title = "滞留天数",
            subTitle = "过期日程会继续显示",
            onClick = onOpenAfterDayDialog,
            text = afterDay,
        ),
    )

    SettingsColumn {
        SettingsGroup(
            title = "数据设置",
            items = dataItems,
        )

        SettingsGroup(
            title = "显示设置",
            items = displayItems,
        )
    }
}


// 输入数字对话框
@Composable
private fun InputNumberDialog(
    mainController: MainController,
    title: String,
    text: String,
    initValue: Int,
    onChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var editValue by remember { mutableStateOf(TextFieldValue(initValue.toString())) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                        onDismiss()
                    } else {
                        errorMessage = "格式校验失败Orz"
                    }
                }
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
internal fun DDLSettingPage(
    mainController: MainController,
    vm: DDLViewModel = hiltViewModel(),
) {

    val afterDay by vm.afterDayFlow.collectAsState(initial = null)

    val beforeDay by vm.beforeDayFlow.collectAsState(initial = null)

    val updateCalendarUrlState by vm.updateLexueCalendarUrlStateLiveData.observeAsState()

    val updateCalendarState by vm.updateLexueCalendarLiveData.observeAsState()

    var showAfterDayDialog by remember { mutableStateOf(false) }

    var showBeforeDayDialog by remember { mutableStateOf(false) }

    DisposableEffect(updateCalendarUrlState) {
        if(updateCalendarUrlState is SimpleState.Success) {
            mainController.snackbar("获取成功")
        } else if(updateCalendarUrlState is SimpleState.Fail) {
            mainController.snackbar("获取失败")
        }
        onDispose { }
    }

    DisposableEffect(updateCalendarState) {
        if(updateCalendarState is SimpleState.Success) {
            mainController.snackbar("拉取成功")
        } else if(updateCalendarState is SimpleState.Fail) {
            mainController.snackbar("拉取失败")
        }
        onDispose { }
    }


    DDLSettingPageContent(
        afterDay = afterDay?.toString() ?: "未设置",
        beforeDay = beforeDay?.toString() ?: "未设置",

        isUpdatingLexueCalendarUrl = updateCalendarUrlState is SimpleState.Loading,
        isUpdatingLexueCalendar = updateCalendarState is SimpleState.Loading,

        onUpdateLexueCalendarUrl = vm::updateLexueCalendarUrl,
        onUpdateLexueCalendar = vm::updateLexueCalendar,

        onOpenAfterDayDialog = { showAfterDayDialog = true },
        onOpenBeforeDayDialog = { showBeforeDayDialog = true },
    )

    if(showBeforeDayDialog) {
        InputNumberDialog(
            mainController = mainController,
            title = "变色天数",
            text = "临近日程会改变颜色",
            initValue = beforeDay?.toInt() ?: 0,
            onChange = { vm.setBeforeDay(it.toLong()) },
            onDismiss = { showBeforeDayDialog = false },
        )
    }

    if(showAfterDayDialog) {
        InputNumberDialog(
            mainController = mainController,
            title = "滞留天数",
            text = "过期日程会继续显示",
            initValue = afterDay?.toInt() ?: 0,
            onChange = { vm.setAfterDay(it.toLong()) },
            onDismiss = { showAfterDayDialog = false },
        )
    }
}