package cn.bit101.android.datastore.basic

import androidx.security.crypto.EncryptedSharedPreferences
import cn.bit101.android.App
import cn.bit101.android.App.Companion.settingsDataStore
import net.gotev.cookiestore.SharedPreferencesCookieStore

internal val ENCRYPTED_SHARED_PREFERENCES = EncryptedSharedPreferences.create(
    "encrypted_store",
    "encrypted_store_key",
    App.context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

internal val SETTING_DATASTORE = App.context.settingsDataStore

internal val COOKIE_PREFERENCES_STORE = SharedPreferencesCookieStore(App.context, "cookie")