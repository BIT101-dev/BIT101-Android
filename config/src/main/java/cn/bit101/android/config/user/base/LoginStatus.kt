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
     * 校园身份已通过验证；不代表教学中心业务会话始终可用
     */
    val schoolConnected: SettingItem<Boolean>

    /**
     * 学校会话是否使用 WebVPN
     */
    val webVpn: SettingItem<Boolean>

    /**
     * Cookie管理器
     */
    val cookieManager: CookieManager

    /**
     * 用于 bit101 的 fake-cookie
     */
    val fakeCookie: SettingItem<String>

    /** 已验证的社区密码摘要，加密存储，与学校密码独立。 */
    val communityPasswordDigest: SettingItem<String>
    val communityRetryAt: SettingItem<Long>
    val communityAttempts: SettingItem<Int>
    val communityNeedsLogin: SettingItem<Boolean>
    /** 已过期 token 的统一认证续期任务，跨进程保留。 */
    val communityRenewalPending: SettingItem<Boolean>
    val communityIdentityRetryAt: SettingItem<Long>
    val communityIdentityAttempts: SettingItem<Int>

    /**
     * 清除校园和社区凭据、Cookie 及重试状态
     */
    suspend fun clear()
}
