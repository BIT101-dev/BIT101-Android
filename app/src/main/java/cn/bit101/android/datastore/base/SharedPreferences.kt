package cn.bit101.android.datastore.base

import androidx.security.crypto.EncryptedSharedPreferences
import cn.bit101.android.App

val ENCRYPTED_SHARED_PREFERENCES = EncryptedSharedPreferences.create(
    "encrypted_store",
    "encrypted_store_key",
    App.context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
