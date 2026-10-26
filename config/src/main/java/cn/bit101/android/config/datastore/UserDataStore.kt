package cn.bit101.android.config.datastore

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import cn.bit101.android.config.datastore.basic.EncryptedPreferencesItem
import cn.bit101.android.config.datastore.basic.Preferences
import cn.bit101.android.config.datastore.basic.PreferencesDataStoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.net.CookieStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UserDataStore @Inject constructor(
    preferences: Preferences
) {
    // Cookie 存储
    val cookieStore: CookieStore = preferences.COOKIE_PREFERENCES_STORE

    // fake-cookie 存储
    private val FAKE_COOKIE = stringPreferencesKey("fake_cookie")
    val fakeCookie = PreferencesDataStoreItem(FAKE_COOKIE, "", preferences.SETTING_DATASTORE)

    val communityRetryAt = PreferencesDataStoreItem(longPreferencesKey("community_retry_at"), 0L, preferences.SETTING_DATASTORE)
    val communityAttempts = PreferencesDataStoreItem(intPreferencesKey("community_attempts"), 0, preferences.SETTING_DATASTORE)
    val communityNeedsLogin = PreferencesDataStoreItem(booleanPreferencesKey("community_needs_login"), false, preferences.SETTING_DATASTORE)
    val communityRenewalPending = PreferencesDataStoreItem(booleanPreferencesKey("community_renewal_pending"), false, preferences.SETTING_DATASTORE)
    val communityIdentityRetryAt = PreferencesDataStoreItem(longPreferencesKey("community_identity_retry_at"), 0L, preferences.SETTING_DATASTORE)
    val communityIdentityAttempts = PreferencesDataStoreItem(intPreferencesKey("community_identity_attempts"), 0, preferences.SETTING_DATASTORE)
    val communityPasswordDigest = EncryptedPreferencesItem(preferences.ENCRYPTED_SHARED_PREFERENCES, "community_password_digest")

    // 登陆状态
    private val LOGIN_STATUS = booleanPreferencesKey("login_status")
    val loginStatus = PreferencesDataStoreItem(LOGIN_STATUS, false, preferences.SETTING_DATASTORE)

    private val LOGIN_WEB_VPN = booleanPreferencesKey("login_web_vpn")
    val loginWebVpn = PreferencesDataStoreItem(LOGIN_WEB_VPN, false, preferences.SETTING_DATASTORE)

    // 下面都是加密存储
    // 登陆学号
    private val LOGIN_SID = "login_sid"
    val loginSid = EncryptedPreferencesItem(preferences.ENCRYPTED_SHARED_PREFERENCES, LOGIN_SID)

    // 密码
    private val LOGIN_PASSWORD = "login_password"
    val loginPassword = EncryptedPreferencesItem(preferences.ENCRYPTED_SHARED_PREFERENCES, LOGIN_PASSWORD)

    init {
        // 在 LoginStatus 和网络客户端可用前完成一次性迁移，保留账号凭据和社区状态。
        val sessionReset = booleanPreferencesKey("school_session_reset_1_4_4")
        runBlocking(Dispatchers.IO) {
            preferences.SETTING_DATASTORE.edit { settings ->
                if (settings[sessionReset] != true) {
                    preferences.clearSchoolCookies()
                    settings.remove(LOGIN_STATUS)
                    settings.remove(LOGIN_WEB_VPN)
                    settings[sessionReset] = true
                }
            }
        }
    }
}
