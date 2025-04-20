package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

interface GallerySettings {
    val hideBotPoster: SettingItem<Boolean>
    val hideBotPosterInSearch: SettingItem<Boolean>
}