package cn.bit101.android.config.setting

import cn.bit101.android.config.common.SettingItem
import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import javax.inject.Inject

internal class DefaultFreeClassroomSettings @Inject constructor(
    settingDataStore: SettingDataStore
) : FreeClassroomSettings{
    override val currentCampus: SettingItem<String> = settingDataStore.freeClassroomCurrentCampus.toSettingItem()

    override val hideBusyClassroom: SettingItem<Boolean> = settingDataStore.freeClassroomHideBusyClassroom.toSettingItem()

    override val freeMinutesThreshold: SettingItem<Long> = settingDataStore.freeClassroomFreeMinutesThreshold.toSettingItem()
}