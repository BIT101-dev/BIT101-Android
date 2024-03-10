package cn.bit101.android.config.user.base

import cn.bit101.android.config.common.SettingItem
import java.net.CookieManager

interface LoginStatus {

    /**
     * 登录学号
     */
    val sid: SettingItem<String>

    /**
     * 登录密码
     */
    val password: SettingItem<String>

    /**
     * 登录状态
     */
    val status: SettingItem<Boolean>

    /**
     * Cookie管理器
     */
    val cookieManager: CookieManager

    /**
     * 用于 bit101 的 fake-cookie
     */
    val fakeCookie: SettingItem<String>

    /**
     * 清除登录状态，包括 sid、password、status、cookie
     */
    suspend fun clear()
}