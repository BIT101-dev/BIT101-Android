package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

sealed interface DarkThemeMode {
    object System : DarkThemeMode
    object Light : DarkThemeMode
    object Dark : DarkThemeMode

    companion object {
        val allModes = listOf(System, Light, Dark)
        fun getMode(modeStr: String) =
            allModes.firstOrNull { it.toDarkThemeData().value == modeStr } ?: allModes.first()
    }
}

data class DarkThemeData(
    val name: String,
    val value: String,
)

fun DarkThemeMode.toDarkThemeData() = when (this) {
    DarkThemeMode.System -> DarkThemeData("跟随系统", "system")
    DarkThemeMode.Light -> DarkThemeData("浅色", "light")
    DarkThemeMode.Dark -> DarkThemeData("深色", "dark")
}

interface ThemeSettings {
    val darkThemeMode: SettingItem<DarkThemeMode>
    val dynamicTheme: SettingItem<Boolean>
    val autoRotate: SettingItem<Boolean>
}