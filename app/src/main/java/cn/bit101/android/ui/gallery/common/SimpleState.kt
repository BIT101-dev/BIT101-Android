package cn.bit101.android.ui.gallery.common

sealed interface SimpleState {
    object Loading : SimpleState
    object Success : SimpleState
    object Error : SimpleState
}

sealed interface SimpleDataState<T> {
    class Loading<T> : SimpleDataState<T>
    data class Success<T>(
        val data: T
    ) : SimpleDataState<T>
    class Error<T> : SimpleDataState<T>
}