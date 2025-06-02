package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

interface DDLSettings {

    /**
     * DDL url
     */
    val url: SettingItem<String>

    /**
     * 变色天数，临近日程会改变颜色
     */
    val beforeDay: SettingItem<Long>

    /**
     * 滞留天数，过期日程会继续显示
     */
    val afterDay: SettingItem<Long>
}