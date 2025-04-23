package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

interface FreeClassroomSettings {
    val currentCampus: SettingItem<String>

    val hideBusyClassroom: SettingItem<Boolean>

    // 最小空闲时间阈值, 不大于该阈值的空闲时间都不被视作空闲
    val freeMinutesThreshold: SettingItem<Long>
}