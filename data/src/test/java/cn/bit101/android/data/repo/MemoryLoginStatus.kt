package cn.bit101.android.data.repo

import cn.bit101.android.config.common.SettingItem
import cn.bit101.android.config.user.base.LoginStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI

internal class MemoryLoginStatus : LoginStatus {
    override val sid = MemorySettingItem("")
    override val password = MemorySettingItem("")
    override val schoolConnected = MemorySettingItem(false)
    override val webVpn = MemorySettingItem(false)
    override val fakeCookie = MemorySettingItem("")
    override val communityPasswordDigest = MemorySettingItem("")
    override val communityRetryAt = MemorySettingItem(0L)
    override val communityAttempts = MemorySettingItem(0)
    override val communityNeedsLogin = MemorySettingItem(false)
    override val communityRenewalPending = MemorySettingItem(false)
    override val communityIdentityRetryAt = MemorySettingItem(0L)
    override val communityIdentityAttempts = MemorySettingItem(0)
    override val cookieManager = CookieManager()

    override suspend fun clear() {
        schoolConnected.set(false)
        webVpn.set(false)
        sid.set("")
        password.set("")
        fakeCookie.set("")
        communityPasswordDigest.set("")
        communityRetryAt.set(0)
        communityAttempts.set(0)
        communityNeedsLogin.set(false)
        communityRenewalPending.set(false)
        communityIdentityRetryAt.set(0)
        communityIdentityAttempts.set(0)
        cookieManager.cookieStore.removeAll()
    }

    suspend fun seedSchool() {
        sid.set("112233")
        password.set("school-password")
        schoolConnected.set(true)
        cookieManager.cookieStore.add(URI("https://sso.bit.edu.cn"), HttpCookie("OLD", "old-session").apply {
            domain = "sso.bit.edu.cn"
            path = "/"
        })
    }
}

internal class MemorySettingItem<T>(initial: T) : SettingItem<T> {
    override val flow = MutableStateFlow(initial)
    override suspend fun get(): T = flow.value
    override suspend fun set(value: T) { flow.value = value }
}
