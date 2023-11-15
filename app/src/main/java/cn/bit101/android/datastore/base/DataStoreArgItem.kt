package cn.bit101.android.datastore.base

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.bit101.android.App
import cn.bit101.android.App.Companion.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class DataStoreArgItem<T>(
    private val baseKey: String,
    private val convertToString: (T) -> String,
    private val convertStringTo: (String) -> T,
) : SettingArgItem<T> {
    override suspend fun get(
        key: String
    ) = withContext(Dispatchers.IO) {
        getFlow(key).first()
    }

    override suspend fun remove(
        key: String
    ) = withContext(Dispatchers.IO) {
        val preferencesKey = stringPreferencesKey(baseKey + key)
        App.context.dataStore.edit { preferences ->
            preferences.remove(preferencesKey)
        }
        return@withContext
    }

    override fun getFlow(key: String): Flow<T> {
        val preferencesKey = stringPreferencesKey(baseKey + key)
        return App.context.dataStore.data
            .map { preferences ->
                val value = preferences[preferencesKey] ?: ""
                convertStringTo(value)
            }
    }

    override suspend fun set(
        key: String, value: T
    ) = withContext(Dispatchers.IO) {
        val preferencesKey = stringPreferencesKey(baseKey + key)
        App.context.dataStore.edit { preferences ->
            preferences[preferencesKey] = convertToString(value)
        }
        return@withContext
    }
}