package cn.bit101.android.config.datastore.basic

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class PreferencesDataStoreItem<T>(
    private val key: Preferences.Key<T>,
    private val initialValue: T,
    private val datastore: DataStore<Preferences>,
) : DataStoreItem<T> {
    override suspend fun get(): T = withContext(Dispatchers.IO) {
        flow.first()
    }

    override suspend fun set(value: T) = withContext(Dispatchers.IO) {
        datastore.edit { preferences ->
            preferences[key] = value
        }
        return@withContext
    }

    override suspend fun remove() = withContext(Dispatchers.IO) {
        datastore.edit { preferences ->
            preferences.remove(key)
        }
        return@withContext
    }

    override val flow: Flow<T>
        get() = datastore.data.map { preferences ->
            preferences[key] ?: initialValue
        }
}