package cn.bit101.android.manager

import android.net.Uri
import cn.bit101.android.datastore.UserDataStore
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.manager.basic.toSettingItem
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpCookie
import javax.inject.Inject

class DefaultLoginStatusManager @Inject constructor(
    private val userDataStore: UserDataStore,
) : LoginStatusManager {
    override val sid = userDataStore.loginSid.toSettingItem()
    override val password = userDataStore.loginPassword.toSettingItem()
    override val status = userDataStore.loginStatus.toSettingItem()


    override val fakeCookie = userDataStore.fakeCookie.toSettingItem()
    override val cookieManager = CookieManager(
        userDataStore.cookieStore,
        CookiePolicy.ACCEPT_ALL
    )

    override suspend fun clear() {
        status.set(false)
        sid.set("")
        password.set("")
        fakeCookie.set("")
        cookieManager.cookieStore.removeAll()
    }
}