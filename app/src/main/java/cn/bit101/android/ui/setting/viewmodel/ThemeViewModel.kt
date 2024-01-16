package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.manager.base.DarkThemeMode
import cn.bit101.android.manager.base.ThemeSettingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeSettingManager: ThemeSettingManager
) : ViewModel() {
    val darkThemeMode = themeSettingManager.darkThemeMode
    val dynamicTheme = themeSettingManager.dynamicTheme
    val autoRotate = themeSettingManager.autoRotate

    fun setDarkThemeMode(mode: DarkThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            themeSettingManager.darkThemeMode.set(mode)
        }
    }

    fun setDynamicTheme(dynamic: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            themeSettingManager.dynamicTheme.set(dynamic)
        }
    }

    fun setAutoRotate(rotate: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            themeSettingManager.autoRotate.set(rotate)
        }
    }
}