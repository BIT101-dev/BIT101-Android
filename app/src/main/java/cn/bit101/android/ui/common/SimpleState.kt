package cn.bit101.android.ui.common

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

/**
 * 简单的错误数据状态，只有三种状态：加载中、成功、失败，其中成功的状态下有数据，失败的状态下有错误信息
 */
sealed interface SimpleErrorDataState<T> {
    class Loading<T> : SimpleErrorDataState<T>
    data class Success<T>(
        val data: T
    ) : SimpleErrorDataState<T>
    data class Fail<T>(
        val msg: String
    ) : SimpleErrorDataState<T>
}