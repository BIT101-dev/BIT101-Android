package cn.bit101.android.config.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.bit101.android.config.datastore.basic.EncryptedPreferencesItem
import cn.bit101.android.config.datastore.basic.Preferences
import cn.bit101.android.config.datastore.basic.PreferencesDataStoreItem
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

    // 登陆状态
    private val LOGIN_STATUS = booleanPreferencesKey("login_status")
    val loginStatus = PreferencesDataStoreItem(LOGIN_STATUS, false, preferences.SETTING_DATASTORE)

    // 下面都是加密存储
    // 登陆学号
    private val LOGIN_SID = "login_sid"
    val loginSid = EncryptedPreferencesItem(preferences.ENCRYPTED_SHARED_PREFERENCES, LOGIN_SID)

    // 密码
    private val LOGIN_PASSWORD = "login_password"
    val loginPassword = EncryptedPreferencesItem(preferences.ENCRYPTED_SHARED_PREFERENCES, LOGIN_PASSWORD)
}