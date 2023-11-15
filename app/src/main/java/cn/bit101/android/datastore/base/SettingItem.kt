package cn.bit101.android.datastore.base

import kotlinx.coroutines.flow.Flow

interface SettingItem<T> {
    suspend fun get(): T
    suspend fun set(value: T)
    suspend fun remove()
    val flow: Flow<T>
}