package cn.bit101.android.config.datastore.basic

import kotlinx.coroutines.flow.Flow

interface DataStoreItem<T> {
    suspend fun get(): T
    suspend fun set(value: T)
    suspend fun remove()
    val flow: Flow<T>
}