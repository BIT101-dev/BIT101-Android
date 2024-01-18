package cn.bit101.android.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.BuildConfig
import cn.bit101.android.manager.base.AboutSettingManager
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.manager.base.PageSettingManager
import cn.bit101.android.repo.base.LoginRepo
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.android.ui.common.withScope
import cn.bit101.api.model.http.app.GetVersionDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
    private val loginRepo: LoginRepo,
    private val aboutSettingManager: AboutSettingManager,
    private val pageSettingManager: PageSettingManager,
    private val loginStatusManager: LoginStatusManager
) : ViewModel() {
    val homePageFlow = pageSettingManager.homePage.flow
    val hidePagesFlow = pageSettingManager.hidePages.flow
    val allPagesFlow = pageSettingManager.allPages.flow

    val loginStatusFlow = loginStatusManager.status.flow

    val lastVersionFlow = aboutSettingManager.lastVersion.flow

    val autoDetectUpgradeFlow = aboutSettingManager.autoDetectUpgrade.flow

    fun logout() = withScope {
        loginRepo.logout()
    }

    fun setLastVersion() = withScope {
        aboutSettingManager.lastVersion.set(BuildConfig.VERSION_CODE.toLong())
    }

    fun setIgnoreVersion(versionCode: Long) = withScope {
        aboutSettingManager.ignoredVersion.set(versionCode)
    }

}