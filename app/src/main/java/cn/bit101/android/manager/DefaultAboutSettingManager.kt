package cn.bit101.android.manager

import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.manager.base.AboutSettingManager
import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.android.manager.basic.toSettingItem
import javax.inject.Inject

class DefaultAboutSettingManager @Inject constructor(
    private val settingDataStore: SettingDataStore
) : AboutSettingManager {

    override val ignoredVersion = settingDataStore.settingIgnoreVersion.toSettingItem()

    override val autoDetectUpgrade = settingDataStore.settingAutoDetectUpgrade.toSettingItem()

    override val lastVersion = settingDataStore.settingLastVersion.toSettingItem()
}