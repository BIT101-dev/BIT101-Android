package cn.bit101.android.features

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.AboutSettings
import cn.bit101.android.config.setting.base.PageSettings
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.android.features.common.helper.withScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val loginRepo: LoginRepo,
    private val aboutSettings: AboutSettings,
    private val pageSettings: PageSettings,
    private val loginStatus: LoginStatus
) : ViewModel() {
    val homePageFlow = pageSettings.homePage.flow
    val hidePagesFlow = pageSettings.hidePages.flow
    val allPagesFlow = pageSettings.allPages.flow

    val loginStatusFlow = loginStatus.status.flow

    val lastVersionFlow = aboutSettings.lastVersion.flow

    val autoDetectUpgradeFlow = aboutSettings.autoDetectUpgrade.flow

    fun logout() = withScope {
        loginRepo.logout()
    }

    fun setLastVersion(versionCode: Long) = withScope {
        aboutSettings.lastVersion.set(versionCode)
    }
}