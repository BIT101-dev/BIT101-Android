package cn.bit101.android.config.setting

import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.DDLSettings
import javax.inject.Inject

internal class DefaultDDLSettings @Inject constructor(
    settingDataStore: SettingDataStore
) : DDLSettings {
    override val afterDay = settingDataStore.ddlScheduleAfterDay.toSettingItem()

    override val beforeDay = settingDataStore.ddlScheduleBeforeDay.toSettingItem()

    override val url = settingDataStore.lexueCalendarUrl.toSettingItem()
}