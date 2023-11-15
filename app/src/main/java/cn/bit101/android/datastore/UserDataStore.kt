package cn.bit101.android.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.bit101.android.datastore.base.DataStoreItem
import cn.bit101.android.datastore.base.ENCRYPTED_SHARED_PREFERENCES
import cn.bit101.android.datastore.base.PreferencesItem

object UserDataStore {

    // 登陆状态
    private val LOGIN_STATUS = booleanPreferencesKey("login_status")
    val loginStatus = DataStoreItem(LOGIN_STATUS, false)

    // BIT101 fake_cookie
    private val FAKE_COOKIE = stringPreferencesKey("fake_cookie")
    val fakeCookie = DataStoreItem(FAKE_COOKIE, "")

    // 下面都是加密存储
    // 登陆学号
    private val LOGIN_SID = "login_sid"
    val loginSid = PreferencesItem(ENCRYPTED_SHARED_PREFERENCES, LOGIN_SID)

    // 密码
    private val LOGIN_PASSWORD = "login_password"
    val loginPassword = PreferencesItem(ENCRYPTED_SHARED_PREFERENCES, LOGIN_PASSWORD)


    suspend fun clear() {
        loginStatus.remove()
        fakeCookie.remove()
        loginSid.remove()
        loginPassword.remove()
    }
}