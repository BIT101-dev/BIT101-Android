package cn.bit101.android.config.user.base

import cn.bit101.android.config.common.SettingItem
import java.net.CookieManager

interface LoginStatus {
    val sid: SettingItem<String>
    val password: SettingItem<String>
    val status: SettingItem<Boolean>

    val cookieManager: CookieManager
    val fakeCookie: SettingItem<String>

    suspend fun clear()
}