package cn.bit101.android.config.setting

import cn.bit101.android.config.common.SettingItem
import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.GallerySettings
import javax.inject.Inject

internal class DefaultGallerySettings @Inject constructor(
    settingDataStore: SettingDataStore
) : GallerySettings {
    override val hideBotPoster: SettingItem<Boolean> = settingDataStore.galleryHideBotPoster.toSettingItem()
    override val hideBotPosterInSearch: SettingItem<Boolean> = settingDataStore.galleryHideBotPosterInSearch.toSettingItem()
}