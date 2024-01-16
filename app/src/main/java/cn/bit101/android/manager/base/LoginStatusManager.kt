package cn.bit101.android.manager.base

import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.android.net.basic.Cookies
import java.net.CookieManager

interface LoginStatusManager {
    val sid: SettingItem<String>
    val password: SettingItem<String>
    val status: SettingItem<Boolean>

    val cookieManager: CookieManager
    val fakeCookie: SettingItem<String>

    suspend fun clear()
}