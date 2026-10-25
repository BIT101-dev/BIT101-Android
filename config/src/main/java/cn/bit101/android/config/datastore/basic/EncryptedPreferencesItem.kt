package cn.bit101.android.config.datastore.basic

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

internal class EncryptedPreferencesItem(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
) : DataStoreItem<String> {
    override suspend fun get(): String {
        return sharedPreferences.getString(key, null) ?: ""
    }

    override suspend fun remove() {
        with(sharedPreferences.edit()) {
            remove(key)
            apply()
        }
    }

    override val flow: Flow<String>
        get() = callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key || changedKey == null) trySend(sharedPreferences.getString(key, null) ?: "")
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            trySend(sharedPreferences.getString(key, null) ?: "")
            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged()

    override suspend fun set(value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }
}