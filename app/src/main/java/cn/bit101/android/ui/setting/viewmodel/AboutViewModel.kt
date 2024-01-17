package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.BuildConfig
import cn.bit101.android.manager.base.AboutSettingManager
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.withSimpleDataStateLiveData
import cn.bit101.android.ui.common.withScope
import cn.bit101.api.model.http.app.GetVersionDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
    private val aboutSettingManager: AboutSettingManager
) : ViewModel() {
    val checkUpdateStateLiveData = MutableLiveData<SimpleDataState<Pair<Boolean, GetVersionDataModel.Response>>?>(null)

    val autoDetectUpgrade = aboutSettingManager.autoDetectUpgrade

    // 这里的强制更新不需要忽略
    fun checkUpdate() = withSimpleDataStateLiveData(checkUpdateStateLiveData) {
        val version = versionRepo.getVersionInfo() ?: throw Exception("get version error")
        val need = version.versionCode > BuildConfig.VERSION_CODE
        Pair(need, version)
    }

    fun setIgnoreVersion(versionCode: Long) = withScope {
        aboutSettingManager.ignoredVersion.set(versionCode)
    }
}