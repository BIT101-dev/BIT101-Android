package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
    val checkUpdateStateLiveData = MutableLiveData<SimpleDataState<GetVersionDataModel.Response>?>(null)

    val autoDetectUpgrade = aboutSettingManager.autoDetectUpgrade

    // 这里的强制更新不需要忽略
    fun checkUpdate() = withSimpleDataStateLiveData(checkUpdateStateLiveData) {
        versionRepo.getVersionInfo()
    }

    fun setIgnoreVersion(versionCode: Long) = withScope {
        aboutSettingManager.ignoredVersion.set(versionCode)
    }
}