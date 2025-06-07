package cn.bit101.android.features.setting.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.component.dialog.InputNumberDialog
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.FreeClassroomSettingData
import cn.bit101.android.features.setting.viewmodel.FreeClassroomViewModel
import cn.bit101.api.model.common.CampusInfo

@Composable
internal fun CampusSelectDialog(
    campusList: List<CampusInfo>,
    campusSelected: CampusInfo,
    loading: Boolean,

    onSetCampus: (CampusInfo) -> Unit,
    onDismiss: () -> Unit,
){
    val selectedCode =
        if (campusList.any { it.code == campusSelected.code })
            campusSelected.code
        else
            campusList.firstOrNull()?.code ?: ""

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "切换校区") },
        text = {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                    campusList.forEach { campus ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .selectable(
                                    selected = (campus.code == selectedCode),
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
                                selected = (campus.code == selectedCode),
                                onClick = null
                            )
                            Text(
                                text = campus.displayName,
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
            text = settingData.currentCampus.displayName,
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
            if (getBuildingTypeStatus is SimpleDataState.Fail || getBuildingTypeStatus == null) {
                vm.loadCampusInfos()
            }
        }

        val campusList = (getBuildingTypeStatus as? SimpleDataState.Success)?.data ?: emptyList()

        CampusSelectDialog(
            campusList = campusList,
            campusSelected = settingData.currentCampus,
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