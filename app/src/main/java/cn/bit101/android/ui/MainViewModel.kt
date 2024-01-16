package cn.bit101.android.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.BuildConfig
import cn.bit101.android.manager.base.AboutSettingManager
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.manager.base.PageSettingManager
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.api.model.http.app.GetVersionDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
    private val aboutSettingManager: AboutSettingManager,
    private val pageSettingManager: PageSettingManager,
    private val loginStatusManager: LoginStatusManager
) : ViewModel() {

    val checkUpdateStateLiveData = MutableLiveData<GetVersionDataModel.Response>(null)

    val homePageFlow = pageSettingManager.homePage.flow
    val hidePagesFlow = pageSettingManager.hidePages.flow
    val allPagesFlow = pageSettingManager.allPages.flow

    val loginStatusFlow = loginStatusManager.status.flow

    init {
        viewModelScope.launch {
            val autoDetectUpgrade = aboutSettingManager.autoDetectUpgrade.get()
            if (autoDetectUpgrade) {
                checkUpdate()
            }
        }
    }

    private fun checkUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val version = versionRepo.getVersionInfo() ?: throw Exception("get version error")
                val ignoreVersion = aboutSettingManager.ignoredVersion.get()
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