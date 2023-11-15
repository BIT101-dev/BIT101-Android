package cn.bit101.android.datastore.base

import kotlinx.coroutines.flow.Flow

interface SettingArgItem<T> {
    suspend fun get(key: String): T
    suspend fun set(key: String, value: T)
    suspend fun remove(key: String)
    fun getFlow(key: String): Flow<T>
}