package cn.bit101.android.features.setting.page

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.GallerySettingData
import cn.bit101.android.features.setting.viewmodel.GalleryViewModel

@Composable
private fun GallerySettingPageContent(
    settingData: GallerySettingData,

    onSettingChange: (GallerySettingData) -> Unit,
) {
    val hideSettings = listOf(
        SettingItemData.Switch(
            title = "在话廊中隐藏机器人 Poster",
            subTitle = "搜索栏、关注列表中的不受影响.\n会导致刷新 / 加载变慢",
            onClick = { onSettingChange(settingData.copy(hideBotPoster = it)) },
            checked = settingData.hideBotPoster,
        ),
        SettingItemData.Switch(
            title = "也隐藏搜索栏中的机器人 Poster",
            subTitle = "未开启隐藏机器人 Poster 时不生效",
            onClick = { onSettingChange(settingData.copy(hideBotPosterInSearch = it)) },
            checked = settingData.hideBotPosterInSearch,
        ),
    )

    SettingsColumn {
        SettingsGroup(
            title = "屏蔽设定",
            items = if(settingData.hideBotPoster) hideSettings.subList(0,2) else hideSettings.subList(0,1)
        )
    }
}

@Composable
internal fun GallerySettingPage() {
    val vm: GalleryViewModel = hiltViewModel()

    val settingData by vm.settingDataFlow.collectAsState(initial = GallerySettingData.default)

    GallerySettingPageContent(
        settingData = settingData,
        onSettingChange = vm::setSettingData)
}