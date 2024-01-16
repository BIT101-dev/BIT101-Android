package cn.bit101.android.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.bit101.android.datastore.basic.COOKIE_PREFERENCES_STORE
import cn.bit101.android.datastore.basic.PreferencesDataStoreItem
import cn.bit101.android.datastore.basic.ENCRYPTED_SHARED_PREFERENCES
import cn.bit101.android.datastore.basic.EncryptedPreferencesItem
import java.net.CookieStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataStore @Inject constructor() {
    // Cookie 存储
    val cookieStore: CookieStore = COOKIE_PREFERENCES_STORE

    // fake-cookie 存储
    private val FAKE_COOKIE = stringPreferencesKey("fake_cookie")
    val fakeCookie = PreferencesDataStoreItem(FAKE_COOKIE, "")

    // 登陆状态
    private val LOGIN_STATUS = booleanPreferencesKey("login_status")
    val loginStatus = PreferencesDataStoreItem(LOGIN_STATUS, false)

    // 下面都是加密存储
    // 登陆学号
    private val LOGIN_SID = "login_sid"
    val loginSid = EncryptedPreferencesItem(ENCRYPTED_SHARED_PREFERENCES, LOGIN_SID)

    // 密码
    private val LOGIN_PASSWORD = "login_password"
    val loginPassword = EncryptedPreferencesItem(ENCRYPTED_SHARED_PREFERENCES, LOGIN_PASSWORD)
}