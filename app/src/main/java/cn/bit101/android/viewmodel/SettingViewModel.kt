package cn.bit101.android.viewmodel

import androidx.lifecycle.ViewModel
import cn.bit101.android.database.DataStore

/**
 * @author flwfdd
 * @date 2023/5/18 下午2:50
 * @description _(:з」∠)_
 */
class SettingViewModel : ViewModel() {
    val rotateFlow = DataStore.settingRotateFlow
    val dynamicThemeFlow = DataStore.settingAutoThemeFlow
    val disableDarkThemeFlow = DataStore.settingDisableDarkThemeFlow

    fun setRotate(rotate: Boolean) {
        DataStore.setBoolean(DataStore.SETTING_ROTATE, rotate)
    }

    fun setDynamicTheme(dynamicTheme: Boolean) {
        DataStore.setBoolean(DataStore.SETTING_DYNAMIC_THEME, dynamicTheme)
    }

    fun setDisableDarkTheme(disableDarkTheme: Boolean) {
        DataStore.setBoolean(DataStore.SETTING_DISABLE_DARK_THEME, disableDarkTheme)
    }
}