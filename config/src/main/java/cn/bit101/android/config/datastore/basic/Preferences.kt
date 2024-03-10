package cn.bit101.android.config.datastore.basic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import net.gotev.cookiestore.SharedPreferencesCookieStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Preferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    /**
     * 加密的 SharedPreferences，用来存储用户名、密码等敏感信息
     */
    val ENCRYPTED_SHARED_PREFERENCES = EncryptedSharedPreferences.create(
        "encrypted_store",
        "encrypted_store_key",
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * 用来存储设置项
     */
    val SETTING_DATASTORE = context.settingsDataStore

    /**
     * 用来存储 Cookie
     */
    val COOKIE_PREFERENCES_STORE = SharedPreferencesCookieStore(context, "cookie")
}