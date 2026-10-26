package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem
import kotlinx.coroutines.flow.Flow

data class LexueSubscription(val ownerSid: String, val url: String)

interface DDLSettings {

    val subscription: Flow<LexueSubscription>
    suspend fun setSubscription(subscription: LexueSubscription)

    /**
     * 变色天数，临近日程会改变颜色
     */
    val beforeDay: SettingItem<Long>

    /**
     * 滞留天数，过期日程会继续显示
     */
    val afterDay: SettingItem<Long>
}