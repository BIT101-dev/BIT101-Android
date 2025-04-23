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
            enable = settingData.hideBotPoster,
        ),
    )

    val viewSettings = listOf(
        SettingItemData.Switch(
            title = "允许横向滑动",
            subTitle = "可以横向滑动来切换选项卡, 可能导致误触",
            onClick = { onSettingChange(settingData.copy(allowHorizontalScroll = it)) },
            checked = settingData.allowHorizontalScroll,
        ),
    )

    SettingsColumn {
        SettingsGroup(
            title = "屏蔽设置",
            items = hideSettings
        )
        SettingsGroup(
            title = "浏览设置",
            items = viewSettings
        )
    }
}

@Composable
internal fun GallerySettingPage() {
    val vm: GalleryViewModel = hiltViewModel()

    val settingData by vm.settingDataFlow.collectAsState(initial = GallerySettingData.default)

    GallerySettingPageContent(
        settingData = settingData,
        onSettingChange = vm::setSettingData
    )
}