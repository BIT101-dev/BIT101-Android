package cn.bit101.android.config.setting

import cn.bit101.android.config.common.Transformer
import cn.bit101.android.config.common.map
import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.DarkThemeMode
import cn.bit101.android.config.setting.base.ThemeSettings
import cn.bit101.android.config.setting.base.toDarkThemeData
import javax.inject.Inject

internal class DefaultThemeSettings @Inject constructor(
    settingDataStore: SettingDataStore
) : ThemeSettings {

    /**
     * darkThemeMode 的转换器
     */
    private val darkThemeModeTransformer = object : Transformer<String, DarkThemeMode> {
        override fun invokeTo(value: String) = DarkThemeMode.getMode(value)
        override fun invokeFrom(value: DarkThemeMode) = value.toDarkThemeData().value
    }

    override val darkThemeMode = settingDataStore.settingDarkTheme.toSettingItem().map(darkThemeModeTransformer)

    override val dynamicTheme = settingDataStore.settingDynamicTheme.toSettingItem()

    override val autoRotate = settingDataStore.settingRotate.toSettingItem()
}