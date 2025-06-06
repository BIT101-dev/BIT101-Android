package cn.bit101.android.features.common.helper

/**
 * 简单的状态，只有三种状态：加载中、成功、失败
 */
sealed interface SimpleState {
    data object Loading : SimpleState
    data object Success : SimpleState
    data object Fail : SimpleState
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


fun SimpleState?.cleared() = when (this) {
    is SimpleState.Success, is SimpleState.Fail -> null
    else -> this
}

fun <T> SimpleDataState<T>?.cleared() = when (this) {
    is SimpleDataState.Success, is SimpleDataState.Fail -> null
    else -> this
}

fun <T> SimpleErrorDataState<T>?.cleared() = when (this) {
    is SimpleErrorDataState.Success, is SimpleErrorDataState.Fail -> null
    else -> this
}