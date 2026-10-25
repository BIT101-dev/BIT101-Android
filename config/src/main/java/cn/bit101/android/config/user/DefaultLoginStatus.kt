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
    override val schoolConnected = userDataStore.loginStatus.toSettingItem()
    override val webVpn = userDataStore.loginWebVpn.toSettingItem()


    override val fakeCookie = userDataStore.fakeCookie.toSettingItem()
    override val communityPasswordDigest = userDataStore.communityPasswordDigest.toSettingItem()
    override val communityRetryAt = userDataStore.communityRetryAt.toSettingItem()
    override val communityAttempts = userDataStore.communityAttempts.toSettingItem()
    override val communityNeedsLogin = userDataStore.communityNeedsLogin.toSettingItem()
    override val communityRenewalPending = userDataStore.communityRenewalPending.toSettingItem()
    override val communityIdentityRetryAt = userDataStore.communityIdentityRetryAt.toSettingItem()
    override val communityIdentityAttempts = userDataStore.communityIdentityAttempts.toSettingItem()
    override val cookieManager = CookieManager(
        userDataStore.cookieStore,
        CookiePolicy.ACCEPT_ALL
    )

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
}
