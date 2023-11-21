package cn.bit101.android.ui.gallery.common

/**
 * 简单的状态，只有三种状态：加载中、成功、失败
 */
sealed interface SimpleState {
    object Loading : SimpleState
    object Success : SimpleState
    object Fail : SimpleState
}

/**
 * 简单的数据状态，只有三种状态：加载中、成功、失败，其中成功的状态下有数据
 */
sealed interface SimpleDataState<T> {
    class Loading<T> : SimpleDataState<T>
    data class Success<T>(
        val data: T
    ) : SimpleDataState<T>
    class Fail<T> : SimpleDataState<T>
}