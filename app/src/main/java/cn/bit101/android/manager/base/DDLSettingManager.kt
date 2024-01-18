package cn.bit101.android.manager.base

import cn.bit101.android.manager.basic.SettingItem

interface DDLSettingManager {
    val url: SettingItem<String>
    val beforeDay: SettingItem<Long>
    val afterDay: SettingItem<Long>
}