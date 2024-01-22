package cn.bit101.android.config.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FlowableSettingItem<T> {
    val flow: Flow<T>
}

interface GettableSettingItem<T> {
    suspend fun get(): T
}

interface SettableSettingItem<T> {
    suspend fun set(value: T)
}

interface SettingItem<T>: GettableSettingItem<T>, SettableSettingItem<T>, FlowableSettingItem<T>

interface Transformer <T, R> {
    fun invokeTo(value: T): R
    fun invokeFrom(value: R): T
}

internal fun <T, R> SettingItem<T>.map(transformer: Transformer<T, R>): SettingItem<R> {
    val item = this
    return object : SettingItem<R> {
        override val flow: Flow<R> = item.flow.map { transformer.invokeTo(it) }
        override suspend fun get() = transformer.invokeTo(item.get())
        override suspend fun set(value: R) = item.set(transformer.invokeFrom(value))
    }
}

internal fun <T> cn.bit101.android.config.datastore.basic.DataStoreItem<T>.toSettingItem(): SettingItem<T> {
    val item = this
    return object : SettingItem<T> {
        override val flow: Flow<T> = item.flow
        override suspend fun get() = item.get()
        override suspend fun set(value: T) = item.set(value)
    }
}
