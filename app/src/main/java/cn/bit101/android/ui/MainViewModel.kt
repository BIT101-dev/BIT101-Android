package cn.bit101.android.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.BuildConfig
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.api.model.http.app.GetVersionDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
) : ViewModel() {

    val checkUpdateStateLiveData = MutableLiveData<GetVersionDataModel.Response>(null)

    init {
        viewModelScope.launch {
            val autoDetectUpgrade = SettingDataStore.settingAutoDetectUpgrade.get()
            if (autoDetectUpgrade) {
                checkUpdate()
            }
        }
    }

    private fun checkUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val version = versionRepo.getVersionInfo() ?: throw Exception("get version error")
                val ignoreVersion = SettingDataStore.settingIgnoreVersion.get()
                val need = version.versionCode.toLong() > ignoreVersion && version.versionCode > BuildConfig.VERSION_CODE
                if(need) checkUpdateStateLiveData.postValue(version)
                else checkUpdateStateLiveData.postValue(null)
            } catch (e: Exception) {
                e.printStackTrace()
                checkUpdateStateLiveData.postValue(null)
            }
        }
    }

}