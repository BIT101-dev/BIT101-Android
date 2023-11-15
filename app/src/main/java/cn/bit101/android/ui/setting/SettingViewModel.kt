package cn.bit101.android.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.App
import cn.bit101.android.BuildConfig
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.api.model.http.app.GetVersionDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CheckUpdateState {
    object Checking : CheckUpdateState
    object Fail: CheckUpdateState

    data class Success(
        val need: Boolean,
        val version: GetVersionDataModel.Response
    ) : CheckUpdateState
}

sealed interface GetLicensesState {
    object Loading : GetLicensesState
    object Fail : GetLicensesState

    data class Success(
        val licenses: String
    ) : GetLicensesState
}

sealed interface UpdateUserInfoState {
    object Loading : UpdateUserInfoState
    object Fail : UpdateUserInfoState

    data class Success(
        val user: GetUserInfoDataModel.Response
    ) : UpdateUserInfoState
}


@HiltViewModel
class SettingViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
) : ViewModel() {
    val checkUpdateStateLiveData = MutableLiveData<CheckUpdateState>(null)
    val getLicensesStateLiveData = MutableLiveData<GetLicensesState>(null)
    val updateUserInfoStateLiveData = MutableLiveData<UpdateUserInfoState>(null)

    // 设置流
    val rotateFlow = SettingDataStore.settingRotate.flow
    val dynamicThemeFlow = SettingDataStore.settingDynamicTheme.flow
    val disableDarkThemeFlow = SettingDataStore.settingDisableDarkTheme.flow
    val settingIgnoreVersionFlow = SettingDataStore.settingIgnoreVersion.flow
    val settingEnableGalleryFlow = SettingDataStore.settingEnableGallery.flow
    val settingUseWebVpnFlow = SettingDataStore.settingUseWebVpn.flow
    val homePageFlow = SettingDataStore.settingHomePage.flow


    // 更新用户信息
    fun updateUserInfo() {
        updateUserInfoStateLiveData.value = UpdateUserInfoState.Loading
        viewModelScope.launch {
            try {
                val res = BIT101API.user.getUserInfo("0").body() ?: throw Exception("getUserInfo error")
                updateUserInfoStateLiveData.postValue(UpdateUserInfoState.Success(res))
            } catch (e: Exception) {
                updateUserInfoStateLiveData.postValue(UpdateUserInfoState.Fail)
                e.printStackTrace()
            }
        }
    }

    fun checkUpdate() {
        checkUpdateStateLiveData.value = CheckUpdateState.Checking
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val version = versionRepo.getVersionInfo() ?: throw Exception("get version error")
                val ignoreVersion = SettingDataStore.settingIgnoreVersion.get()
                val need = version.versionCode.toLong() > ignoreVersion && version.versionCode > BuildConfig.VERSION_CODE
                checkUpdateStateLiveData.postValue(CheckUpdateState.Success(need, version))
            } catch (e: Exception) {
                checkUpdateStateLiveData.postValue(CheckUpdateState.Fail)
            }
        }
    }

    // 获取开源声明
    fun getLicenses() {
        getLicensesStateLiveData.value = GetLicensesState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val input = App.context.assets.open("open_source_licenses.txt")
                val buffer = ByteArray(input.available())
                input.read(buffer)
                input.close()
                val res = String(buffer)
                getLicensesStateLiveData.postValue(GetLicensesState.Success(res))
            } catch (e: Exception) {
                getLicensesStateLiveData.postValue(GetLicensesState.Fail)
                e.printStackTrace()
            }
        }
    }

    fun setDynamicTheme(value: Boolean) {
        viewModelScope.launch {
            SettingDataStore.settingDynamicTheme.set(value)
        }
    }
    fun setRotate(value: Boolean) {
        viewModelScope.launch {
            SettingDataStore.settingRotate.set(value)
        }
    }
    fun setDisableDarkTheme(value: Boolean) {
        viewModelScope.launch {
            SettingDataStore.settingDisableDarkTheme.set(value)
        }
    }

    fun setIgnore(versionCode: Long) {
        viewModelScope.launch {
            SettingDataStore.settingIgnoreVersion.set(versionCode)
        }
    }

    fun setEnableGallery(value: Boolean) {
        viewModelScope.launch {
            SettingDataStore.settingEnableGallery.set(value)
        }
    }

    fun setUseWebVpn(value: Boolean) {
        viewModelScope.launch {
            SettingDataStore.settingUseWebVpn.set(value)
        }
    }

    fun setHomePage(value: String) {
        viewModelScope.launch {
            SettingDataStore.settingHomePage.set(value)
        }
    }


}