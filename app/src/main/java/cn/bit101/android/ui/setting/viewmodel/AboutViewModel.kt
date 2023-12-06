package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.BuildConfig
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.api.model.http.app.GetVersionDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
) : ViewModel() {
    val checkUpdateStateLiveData = MutableLiveData<SimpleDataState<Pair<Boolean, GetVersionDataModel.Response>>>(null)


    // 这里的强制更新不需要忽略
    fun checkUpdate() {
        checkUpdateStateLiveData.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val version = versionRepo.getVersionInfo() ?: throw Exception("get version error")
                val need = version.versionCode > BuildConfig.VERSION_CODE
                checkUpdateStateLiveData.postValue(SimpleDataState.Success(Pair(need, version)))
            } catch (e: Exception) {
                checkUpdateStateLiveData.postValue(SimpleDataState.Fail())
            }
        }
    }

    fun setIgnoreVersion(versionCode: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.settingIgnoreVersion.set(versionCode)
        }
    }
}