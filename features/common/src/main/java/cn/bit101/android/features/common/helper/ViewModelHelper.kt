package cn.bit101.android.features.common.helper

import androidx.lifecycle.MutableLiveData
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.base.LoginException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** 在调用时设置 Loading，防止 UI 状态发布前重复启动同一操作。 */
@MainThread
fun <T> ViewModel.withSimpleErrorDataState(
    state: MutableStateFlow<SimpleErrorDataState<T>?>,
    cancelledState: SimpleErrorDataState<T>? = null,
    block: suspend () -> T,
): Job? {
    if (state.value is SimpleErrorDataState.Loading) return null
    state.value = SimpleErrorDataState.Loading()
    return viewModelScope.launch {
        try {
            state.value = SimpleErrorDataState.Success(block())
        } catch (e: CancellationException) {
            state.value = cancelledState
            throw e
        } catch (e: Exception) {
            state.value = SimpleErrorDataState.Fail(when (e) {
                is LoginException -> e.message ?: "认证失败，请稍后重试。"
                is IOException -> "网络连接失败，请稍后重试。"
                else -> "操作失败，请稍后重试。"
            })
        }
    }
}

fun ViewModel.withSimpleStateFlow(
    stateFlow: MutableStateFlow<SimpleState?>,
    block: suspend () -> Unit
) {
    viewModelScope.launch(Dispatchers.IO) {
        stateFlow.value = SimpleState.Loading
        runCatching {
            block()
            stateFlow.value = SimpleState.Success
        }.onFailure {
            it.printStackTrace()
            stateFlow.value = SimpleState.Fail
        }
    }
}

fun ViewModel.withSimpleStateLiveData(
    stateLiveData: MutableLiveData<SimpleState?>,
    block: suspend () -> Unit
) {
    viewModelScope.launch(Dispatchers.IO) {
        stateLiveData.postValue(SimpleState.Loading)
        runCatching {
            block()
            stateLiveData.postValue(SimpleState.Success)
        }.onFailure {
            it.printStackTrace()
            stateLiveData.postValue(SimpleState.Fail)
        }
    }
}

fun <T> ViewModel.withSimpleDataStateFlow(
    stateFlow: MutableStateFlow<SimpleDataState<T>?>,
    block: suspend () -> T
) {
    viewModelScope.launch(Dispatchers.IO) {
        stateFlow.value = SimpleDataState.Loading()
        runCatching {
            val res = block()
            stateFlow.value = SimpleDataState.Success(res)
        }.onFailure {
            it.printStackTrace()
            stateFlow.value = SimpleDataState.Fail()
        }
    }
}

fun <T> ViewModel.withSimpleDataStateLiveData(
    stateLiveData: MutableLiveData<SimpleDataState<T>?>,
    block: suspend () -> T
) {
    viewModelScope.launch(Dispatchers.IO) {
        stateLiveData.postValue(SimpleDataState.Loading())
        runCatching {
            val res = block()
            stateLiveData.postValue(SimpleDataState.Success(res))
        }.onFailure {
            it.printStackTrace()
            stateLiveData.postValue(SimpleDataState.Fail())
        }
    }
}

fun ViewModel.withScope(
    block: suspend () -> Unit
) {
    viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            block()
        }
    }
}
