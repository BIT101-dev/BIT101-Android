package cn.bit101.android.manager.base

import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.api.model.common.NameAndValue

sealed interface DarkThemeMode {
    object System : DarkThemeMode
    object Light : DarkThemeMode
    object Dark : DarkThemeMode

    companion object {
        val allModes = listOf(System, Light, Dark)
        fun getMode(modeStr: String) =
            allModes.firstOrNull { it.toNameAndValue().value == modeStr } ?: allModes.first()
    }
}

fun DarkThemeMode.toNameAndValue() = when (this) {
    DarkThemeMode.System -> NameAndValue("跟随系统", "system")
    DarkThemeMode.Light -> NameAndValue("浅色", "light")
    DarkThemeMode.Dark -> NameAndValue("深色", "dark")
}

interface ThemeSettingManager {
    val darkThemeMode: SettingItem<DarkThemeMode>
    val dynamicTheme: SettingItem<Boolean>
    val autoRotate: SettingItem<Boolean>
}