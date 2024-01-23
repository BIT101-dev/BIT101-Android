package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.AboutSettings
import cn.bit101.android.data.repo.base.VersionRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleDataStateLiveData
import cn.bit101.api.model.http.app.GetVersionDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
    private val aboutSettings: AboutSettings
) : ViewModel() {
    val checkUpdateStateLiveData = MutableLiveData<SimpleDataState<GetVersionDataModel.Response>?>(null)

    val autoDetectUpgrade = aboutSettings.autoDetectUpgrade

    // 这里的强制更新不需要忽略
    fun checkUpdate() = withSimpleDataStateLiveData(checkUpdateStateLiveData) {
        versionRepo.getVersionInfo()
    }

    fun setIgnoreVersion(versionCode: Long) = withScope {
        aboutSettings.ignoredVersion.set(versionCode)
    }
}