package cn.bit101.android.features.common.helper

import androidx.lifecycle.MutableLiveData
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.CourseSyncException
import cn.bit101.android.data.repo.CourseSyncResult
import cn.bit101.android.data.repo.base.SchoolConnectionResult
import cn.bit101.android.data.repo.base.LoginException
import cn.bit101.api.service.school.SchoolAccessDenied
import cn.bit101.api.service.school.SchoolResponseError
import cn.bit101.api.service.school.SchoolSessionExpired
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
    failureMessage: (Exception) -> String = { it.operationErrorMessage() },
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
            state.value = SimpleErrorDataState.Fail(failureMessage(e))
        }
    }
}

fun Exception.operationErrorMessage(): String = when (this) {
    is CourseSyncException -> "$step 失败：${(cause as Exception).operationErrorMessage()}"
    is SchoolAccessDenied, is SchoolResponseError, is SchoolSessionExpired -> message.orEmpty()
    is LoginException -> message ?: "认证失败，请稍后重试。"
    is IOException -> "网络连接失败，请稍后重试。"
    else -> "操作失败，请稍后重试。"
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
            if (it is CancellationException) throw it
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
            if (it is CancellationException) throw it
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
            if (it is CancellationException) throw it
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
            if (it is CancellationException) throw it
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

fun CourseSyncResult.successMessage(): String = examFailure?.let {
    "课程已更新，考试更新失败：${it.operationErrorMessage()}。已有考试安排已保留。"
} ?: "课表刷新成功"

fun SchoolConnectionResult.successMessage(): String = teachingCenterWarning?.let {
    "校园登录成功，教务暂不可用"
} ?: "校园登录成功"
