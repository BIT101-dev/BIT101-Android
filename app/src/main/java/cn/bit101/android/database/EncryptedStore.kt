package cn.bit101.android.database

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import cn.bit101.android.App

/**
 * @author flwfdd
 * @date 13/05/2023 22:41
 * @description 加密保存数据
 * _(:з」∠)_
 */
class EncryptedStore {
    companion object {
        const val SID = "sid"
        const val PASSWORD = "password"

        private const val sharedPrefsFile: String = "encrypted_store"
        private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            sharedPrefsFile,
            "encrypted_store_key",
            App.context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        fun getString(key: String): String? {
            return sharedPreferences.getString(key, null)
        }

        fun setString(key: String, value: String) {
            with(sharedPreferences.edit())
            {
                putString(key, value)
                apply()
            }
        }

        fun deleteString(key: String) {
            with(sharedPreferences.edit())
            {
                remove(key)
                apply()
            }
        }
    }
}