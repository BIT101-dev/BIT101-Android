package cn.bit101.android.manager

import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.manager.base.DDLSettingManager
import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.android.manager.basic.toSettingItem
import javax.inject.Inject

class DefaultDDLSettingManager @Inject constructor(
    private val settingDataStore: SettingDataStore
) : DDLSettingManager {

    override val afterDay = settingDataStore.ddlScheduleAfterDay.toSettingItem()

    override val beforeDay = settingDataStore.ddlScheduleBeforeDay.toSettingItem()

    override val url = settingDataStore.lexueCalendarUrl.toSettingItem()
}