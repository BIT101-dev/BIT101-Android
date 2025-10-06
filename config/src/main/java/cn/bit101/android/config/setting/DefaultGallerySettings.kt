package cn.bit101.android.config.setting

import cn.bit101.android.config.common.SettingItem
import cn.bit101.android.config.common.Transformer
import cn.bit101.android.config.common.map
import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.GallerySettings
import javax.inject.Inject

internal class DefaultGallerySettings @Inject constructor(
    settingDataStore: SettingDataStore
) : GallerySettings {
    /**
     * UID 列表的转换器，用于将字符串转换为 UID 列表，或者将 UID 列表转换为字符串
     */
    private val uidSetTransformer = object : Transformer<String, List<Int>> {
        override fun invokeTo(value: String): List<Int> {
            if (value.isEmpty()) {
                return emptyList()
            }
            return value.split(',').map { it.toInt() }.toList()
        }

        override fun invokeFrom(value: List<Int>): String {
            return value.joinToString(separator = ",")
        }
    }

    override val hideBotPoster: SettingItem<Boolean> = settingDataStore.galleryHideBotPoster.toSettingItem()
    override val hideBotPosterInSearch: SettingItem<Boolean> = settingDataStore.galleryHideBotPosterInSearch.toSettingItem()
    override val hideUserUids: SettingItem<List<Int>> = settingDataStore.galleryHideUserUidList.toSettingItem().map(uidSetTransformer)

    override val allowHorizontalScroll: SettingItem<Boolean> = settingDataStore.galleryAllowHorizonalScroll.toSettingItem()
}