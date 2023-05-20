package cn.bit101.android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.App
import cn.bit101.android.BuildConfig
import cn.bit101.android.database.DataStore
import cn.bit101.android.net.VersionInfo
import cn.bit101.android.net.bit101.BIT101Service
import cn.bit101.android.net.bit101.UserInfoResponse
import cn.bit101.android.net.getVersionInfo
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 2023/5/18 下午2:50
 * @description _(:з」∠)_
 */
class SettingViewModel : ViewModel() {
    var userInfo: UserInfoResponse? = null
    var versionInfo: VersionInfo? = null

    // 忽略的版本号
    val ignoreVersionFlow = DataStore.settingIgnoreVersionFlow

    init {
        viewModelScope.launch {
            try {
                BIT101Service.service.userInfo(0).body()?.let {
                    Log.i("SettingViewModel", "getUserInfo $it")
                    userInfo = it.copy(avatar = it.avatar + "!low") // 转为低分辨率链接
                }
            } catch (e: Exception) {
                Log.i("SettingViewModel", "getUserInfo error ${e.message}")
            }
        }
    }

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

    // 获取开源声明
    fun getLicenses(): String {
        val input = App.context.assets.open("open_source_licenses.txt")
        val buffer = ByteArray(input.available())
        input.read(buffer)
        input.close()
        return String(buffer)
    }

    // 检查是否有更新
    suspend fun checkUpdate(): Boolean {
        versionInfo = getVersionInfo() ?: return false
        return versionInfo!!.version_code > BuildConfig.VERSION_CODE
    }

    // 忽略更新
    fun ignoreUpdate() {
        versionInfo?.let {
            DataStore.setLong(DataStore.SETTING_IGNORE_VERSION, it.version_code.toLong())
        }
    }
}