package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

/**
 * 黑暗主题模式
 */
sealed interface DarkThemeMode {

    /**
     * 跟随系统
     */
    object System : DarkThemeMode

    /**
     * 浅色
     */
    object Light : DarkThemeMode

    /**
     * 深色
     */
    object Dark : DarkThemeMode

    companion object {

        /**
         * 所有模式
         */
        val allModes = listOf(System, Light, Dark)

        /**
         * 通过字符串获取模式
         */
        fun getMode(modeStr: String) =
            allModes.firstOrNull { it.toDarkThemeData().value == modeStr } ?: allModes.first()
    }
}


/**
 * 黑暗主题数据，包含黑暗主题名字和黑暗主题在 datastore 中的值
 */
data class DarkThemeData(

    /**
     * 黑暗主题名字
     */
    val name: String,

    /**
     * 黑暗主题在 datastore 中的值
     */
    val value: String,
)


/**
 * 将 [DarkThemeMode] 转换为 [DarkThemeData]
 */
fun DarkThemeMode.toDarkThemeData() = when (this) {
    DarkThemeMode.System -> DarkThemeData("跟随系统", "system")
    DarkThemeMode.Light -> DarkThemeData("浅色", "light")
    DarkThemeMode.Dark -> DarkThemeData("深色", "dark")
}

interface ThemeSettings {

    /**
     * 黑暗主题模式
     */
    val darkThemeMode: SettingItem<DarkThemeMode>

    /**
     * 动态主题
     */
    val dynamicTheme: SettingItem<Boolean>

    /**
     * 自动旋转
     */
    val autoRotate: SettingItem<Boolean>
}