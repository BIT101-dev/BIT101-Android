package cn.bit101.android.config.setting

import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.AboutSettings
import javax.inject.Inject

internal class DefaultAboutSettings @Inject constructor(
    settingDataStore: SettingDataStore
) : AboutSettings {
    override val ignoredVersion = settingDataStore.settingIgnoreVersion.toSettingItem()
    override val autoDetectUpgrade = settingDataStore.settingAutoDetectUpgrade.toSettingItem()
    override val lastVersion = settingDataStore.settingLastVersion.toSettingItem()
}