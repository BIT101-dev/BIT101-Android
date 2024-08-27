package cn.bit101.android.config.user

import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.UserDataStore
import cn.bit101.android.config.user.base.LoginStatus
import java.net.CookieManager
import java.net.CookiePolicy
import javax.inject.Inject

internal class DefaultLoginStatus @Inject constructor(
    userDataStore: UserDataStore
) : LoginStatus {
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