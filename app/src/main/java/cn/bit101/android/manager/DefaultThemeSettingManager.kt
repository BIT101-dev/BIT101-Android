package cn.bit101.android.manager

import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.manager.base.DarkThemeMode
import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.android.manager.base.ThemeSettingManager
import cn.bit101.android.manager.base.toNameAndValue
import cn.bit101.android.manager.basic.Transformer
import cn.bit101.android.manager.basic.map
import cn.bit101.android.manager.basic.toSettingItem
import javax.inject.Inject

class DefaultThemeSettingManager @Inject constructor(
    private val settingDataStore: SettingDataStore
) : ThemeSettingManager {

    private val darkThemeModeTransformer = object : Transformer<String, DarkThemeMode> {
        override fun invokeTo(value: String) = DarkThemeMode.getMode(value)
        override fun invokeFrom(value: DarkThemeMode) = value.toNameAndValue().value
    }

    override val darkThemeMode = settingDataStore.settingDarkTheme.toSettingItem().map(darkThemeModeTransformer)

    override val dynamicTheme = settingDataStore.settingDynamicTheme.toSettingItem()

    override val autoRotate = settingDataStore.settingRotate.toSettingItem()
}