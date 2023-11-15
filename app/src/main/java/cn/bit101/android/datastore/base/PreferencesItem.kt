package cn.bit101.android.datastore.base

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class PreferencesItem(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
) : SettingItem<String> {
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
        get() = flowOf(sharedPreferences.getString(key, null) ?: "")

    override suspend fun set(value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }
}