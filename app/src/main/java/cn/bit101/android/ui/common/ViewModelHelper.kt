package cn.bit101.android.ui.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
            stateFlow.value = SimpleState.Fail
        }
    }
}

fun ViewModel.withSimpleStateLiveData(
    stateLiveData: MutableLiveData<SimpleState>,
    block: suspend () -> Unit
) {
    viewModelScope.launch(Dispatchers.IO) {
        stateLiveData.postValue(SimpleState.Loading)
        runCatching {
            block()
            stateLiveData.postValue(SimpleState.Success)
        }.onFailure {
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
            stateFlow.value = SimpleDataState.Fail()
        }
    }
}

fun <T> ViewModel.withSimpleDataStateLiveData(
    stateLiveData: MutableLiveData<SimpleDataState<T>>,
    block: suspend () -> T
) {
    viewModelScope.launch(Dispatchers.IO) {
        stateLiveData.postValue(SimpleDataState.Loading())
        runCatching {
            val res = block()
            stateLiveData.postValue(SimpleDataState.Success(res))
        }.onFailure {
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