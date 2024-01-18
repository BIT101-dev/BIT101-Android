package cn.bit101.android.manager.base

import cn.bit101.android.manager.basic.SettingItem

interface AboutSettingManager {
    val ignoredVersion: SettingItem<Long>
    val autoDetectUpgrade: SettingItem<Boolean>
}