package cn.bit101.android.manager

import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.manager.base.MapSettingManager
import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.android.manager.basic.toSettingItem
import javax.inject.Inject

class DefaultMapSettingManager @Inject constructor(
    private val settingDataStore: SettingDataStore
) : MapSettingManager {
    override val scale = settingDataStore.mapScale.toSettingItem()
}