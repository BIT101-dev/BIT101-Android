package cn.bit101.android.features

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.AboutSettings
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.data.repo.base.CommunityLoginRepo
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.poster.ImageDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val communityLogin: CommunityLoginRepo,
    private val loginRepo: SchoolLoginRepo,
    private val aboutSettings: AboutSettings,
    val smsCodeRequestHub: SmsCodeRequestHub,
) : ViewModel() {

    val lastVersionFlow = aboutSettings.lastVersion.flow
    val autoDetectUpgradeFlow = aboutSettings.autoDetectUpgrade.flow

    // 图片保存工具类
    val imageDownloader = ImageDownloader()

    suspend fun autoLoginCommunity() = communityLogin.autoLogin()

    fun logout() = withScope {
        loginRepo.logout()
    }

    fun setLastVersion(versionCode: Long) = withScope {
        aboutSettings.lastVersion.set(versionCode)
    }
}
