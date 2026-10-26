package cn.bit101.android.config.setting

import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.config.setting.base.LexueSubscription
import javax.inject.Inject

internal class DefaultDDLSettings @Inject constructor(
    private val settingDataStore: SettingDataStore
) : DDLSettings {
    override val afterDay = settingDataStore.ddlScheduleAfterDay.toSettingItem()

    override val beforeDay = settingDataStore.ddlScheduleBeforeDay.toSettingItem()

    override val subscription = settingDataStore.lexueSubscription
    override suspend fun setSubscription(subscription: LexueSubscription) = settingDataStore.setLexueSubscription(subscription)
}