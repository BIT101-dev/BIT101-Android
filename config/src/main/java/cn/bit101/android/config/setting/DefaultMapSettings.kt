package cn.bit101.android.config.setting

import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.MapSettings
import javax.inject.Inject

internal class DefaultMapSettings @Inject constructor(
    settingDataStore: SettingDataStore
) : MapSettings {
    override val scale = settingDataStore.mapScale.toSettingItem()
}