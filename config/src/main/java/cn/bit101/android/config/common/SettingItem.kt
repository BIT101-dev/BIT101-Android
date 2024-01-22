package cn.bit101.android.config.common

import cn.bit101.android.config.datastore.basic.DataStoreItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 设置项，可以读写，也可以监听
 */
interface SettingItem<T> {
    val flow: Flow<T>
    suspend fun get(): T
    suspend fun set(value: T)
}

/**
 * 用于转换数据类型，T <-> R
 */
internal interface Transformer <T, R> {
    fun invokeTo(value: T): R
    fun invokeFrom(value: R): T
}

/**
 * 用于转换数据类型
 */
internal fun <T, R> SettingItem<T>.map(transformer: Transformer<T, R>): SettingItem<R> {
    val item = this
    return object : SettingItem<R> {
        override val flow: Flow<R> = item.flow.map { transformer.invokeTo(it) }
        override suspend fun get() = transformer.invokeTo(item.get())
        override suspend fun set(value: R) = item.set(transformer.invokeFrom(value))
    }
}

/**
 * 从 DataStoreItem 转换为 SettingItem
 */
internal fun <T> DataStoreItem<T>.toSettingItem(): SettingItem<T> {
    val item = this
    return object : SettingItem<T> {
        override val flow: Flow<T> = item.flow
        override suspend fun get() = item.get()
        override suspend fun set(value: T) = item.set(value)
    }
}
