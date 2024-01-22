package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.DarkThemeMode
import cn.bit101.android.config.setting.base.ThemeSettings
import cn.bit101.android.ui.common.withScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeSettings: ThemeSettings
) : ViewModel() {
    val darkThemeMode = themeSettings.darkThemeMode
    val dynamicTheme = themeSettings.dynamicTheme
    val autoRotate = themeSettings.autoRotate

    fun setDarkThemeMode(mode: DarkThemeMode) = withScope {
        themeSettings.darkThemeMode.set(mode)
    }

    fun setDynamicTheme(dynamic: Boolean) = withScope {
        themeSettings.dynamicTheme.set(dynamic)
    }

    fun setAutoRotate(rotate: Boolean) = withScope {
        themeSettings.autoRotate.set(rotate)
    }
}