package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

interface AboutSettings {
    val ignoredVersion: SettingItem<Long>
    val autoDetectUpgrade: SettingItem<Boolean>
    val lastVersion: SettingItem<Long>
}