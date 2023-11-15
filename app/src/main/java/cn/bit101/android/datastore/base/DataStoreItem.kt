package cn.bit101.android.datastore.base

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.bit101.android.App
import cn.bit101.android.App.Companion.dataStore
import com.google.gson.annotations.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class DataStoreItem<T>(
    private val key: Preferences.Key<T>,
    private val initialValue: T,
) : SettingItem<T> {
    override suspend fun get(): T = withContext(Dispatchers.IO) {
        flow.first()
    }

    override suspend fun set(value: T) = withContext(Dispatchers.IO) {
        App.context.dataStore.edit { preferences ->
            preferences[key] = value
        }
        return@withContext
    }

    override suspend fun remove() = withContext(Dispatchers.IO) {
        App.context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
        return@withContext
    }

    override val flow: Flow<T>
        get() = App.context.dataStore.data.map { preferences ->
            preferences[key] ?: initialValue
        }
}