package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

interface DDLSettings {
    val url: SettingItem<String>
    val beforeDay: SettingItem<Long>
    val afterDay: SettingItem<Long>
}