package cn.bit101.android.features.setting.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.FreeClassroomSettingData
import cn.bit101.android.features.setting.viewmodel.FreeClassroomViewModel

@Composable
internal fun CampusSelectDialog(
    campusList: List<String>,
    campusSelect: String,
    loading: Boolean,

    onSetCampus: (String) -> Unit,
    onDismiss: () -> Unit,
){
    val selectedOption = if(campusList.contains(campusSelect)) campusSelect
    else campusList.firstOrNull() ?: ""

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "切换校区") },
        text = {
            if(loading){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }else{
                val scrollState = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxSize()
                        .selectableGroup()
                        .verticalScroll(scrollState)
                ) {
                    campusList.forEach { campus ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .selectable(
                                    selected = (campus == selectedOption),
                                    onClick = {
                                        onSetCampus(campus)
                                        onDismiss()
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (campus == selectedOption),
                                onClick = null
                            )
                            Text(
                                text = campus,
                                modifier = Modifier.padding(start = 10.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
    )
}

// 输入数字对话框 (也是从 DDL 那边 copy 来的)
@Composable
private fun InputNumberDialog(
    title: String,
    text: String,
    initValue: Int,
    onChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSnackBar: (String) -> Unit,
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
                        onSnackBar("设置成功OvO")
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
private fun FreeClassroomSettingPageContent(
    settingData: FreeClassroomSettingData,

    onOpenEditCampusDialog: () -> Unit,
    onOpenFreeMinutesThresholdInputDialog: () -> Unit,

    onSettingChange: (FreeClassroomSettingData) -> Unit,
) {
    val hideSettings = listOf(
        SettingItemData.Button(
            title = "当前校区",
            text = settingData.currentCampus,
            onClick = onOpenEditCampusDialog,
        ),
        SettingItemData.Switch(
            title = "隐藏当前不空闲的教室",
            onClick = { onSettingChange(settingData.copy(hideBusyClassroom = it)) },
            checked = settingData.hideBusyClassroom,
        ),
        SettingItemData.Button(
            title = "空闲时段阈值",
            subTitle = "不大于此值的空闲时段将被忽略",
            text = "${settingData.freeMinutesThreshold} 分钟",
            onClick = onOpenFreeMinutesThresholdInputDialog,
        ),
    )

    SettingsColumn {
        SettingsGroup(
            title = "空教室查询设置",
            subTitle = "如果获取失败可以尝试在账号设置中“检查登录状态”哦",
            items = hideSettings
        )
    }
}

@Composable
internal fun FreeClassroomSettingPage(
    onSnackBar: (String) -> Unit,
) {
    val vm: FreeClassroomViewModel=hiltViewModel()

    val settingData by vm.settingDataFlow.collectAsState(initial = FreeClassroomSettingData.default)

    val changeSettingStatus by vm.changeSettingStatusLiveData.observeAsState()

    val getBuildingTypeStatus by vm.getBuildingTypeStatusLiveData.observeAsState()

    var showCampusSelectDialog by rememberSaveable { mutableStateOf(false) }
    var showFreeMinutesThresholdInputDialog by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(changeSettingStatus){
        if(changeSettingStatus is SimpleState.Success){
            onSnackBar("设置成功")
        } else if(changeSettingStatus is SimpleState.Fail) {
            onSnackBar("设置失败")
        }
        onDispose {}
    }

    if (showCampusSelectDialog) {
        LaunchedEffect(getBuildingTypeStatus){
            if(getBuildingTypeStatus is SimpleDataState.Fail || getBuildingTypeStatus==null){
                vm.loadBuildingTypes()
            }
        }

        val campusList=(getBuildingTypeStatus as? SimpleDataState.Success)?.data ?: emptyList()

        CampusSelectDialog(
            campusList = campusList,
            campusSelect = settingData.currentCampus,
            loading = getBuildingTypeStatus is SimpleDataState.Loading,

            onSetCampus = { campus ->
                vm.setCampus(campus)
                showCampusSelectDialog = false
            },
            onDismiss = { showCampusSelectDialog = false }
        )
    }

    if (showFreeMinutesThresholdInputDialog) {
        InputNumberDialog(
            title = "空闲时段阈值",
            text = "不大于此值的空闲时段将被忽略 (单位: 分钟)",
            initValue = settingData.freeMinutesThreshold.toInt(),
            onChange = { vm.setFreeMinutesThreshold(it.toLong()) },
            onDismiss = { showFreeMinutesThresholdInputDialog = false },
            onSnackBar = onSnackBar
        )
    }

    FreeClassroomSettingPageContent(
        settingData = settingData,
        onOpenEditCampusDialog = { showCampusSelectDialog = true },
        onOpenFreeMinutesThresholdInputDialog = { showFreeMinutesThresholdInputDialog = true },
        onSettingChange = vm::setSettingData
    )
}