package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

interface AboutSettings {

    /**
     * 版本号
     */
    val ignoredVersion: SettingItem<Long>

    /**
     * 是否自动检测更新
     */
    val autoDetectUpgrade: SettingItem<Boolean>

    /**
     * 最后一个版本号，用于判断是否需要显示更新提示
     */
    val lastVersion: SettingItem<Long>
}