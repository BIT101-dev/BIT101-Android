package cn.bit101.android.features

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.AboutSettings
import cn.bit101.android.config.setting.base.PageSettings
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.config.setting.base.toPageData
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.android.features.common.helper.withScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val loginRepo: LoginRepo,
    private val aboutSettings: AboutSettings,
    private val pageSettings: PageSettings,
    private val loginStatus: LoginStatus
) : ViewModel() {

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