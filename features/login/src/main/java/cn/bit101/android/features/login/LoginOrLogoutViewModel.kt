package cn.bit101.android.features.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.api.model.common.SmsCodeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

// 等待用户输入的短信验证码请求
internal class PendingSmsCodeRequest(
    val maskedPhone: String,
    private val deferred: CompletableDeferred<String>,
) {
    fun submit(code: String) {
        deferred.complete(code)
    }

    fun cancel() {
        deferred.cancel()
    }
}

@HiltViewModel
internal class LoginOrLogoutViewModel @Inject constructor(
    private val loginStatus: LoginStatus,
    private val loginRepo: LoginRepo
) : ViewModel() {
    // 过程的状态
    val checkLoginStateLiveData = MutableLiveData<SimpleState?>(null)
    val postLoginStateLiveData = MutableLiveData<SimpleState?>(null)

    // 等待用户输入短信验证码的请求
    val pendingSmsCodeRequestLiveData = MutableLiveData<PendingSmsCodeRequest?>(null)

    val sidFlow = loginStatus.sid.flow

    // 检查登录状态
    fun checkLoginState() = withSimpleStateLiveData(checkLoginStateLiveData) {
        val res = loginRepo.checkLogin()
        if(!res) throw Exception("check login error")
    }

    // 登录
    fun login(username: String, password: String) = withSimpleStateLiveData(postLoginStateLiveData) {
        val smsCodeHandler = SmsCodeHandler { request: SmsCodeRequest ->
            val deferred = CompletableDeferred<String>()
            pendingSmsCodeRequestLiveData.postValue(
                PendingSmsCodeRequest(request.maskedPhone, deferred)
            )
            try {
                deferred.await()
            } finally {
                pendingSmsCodeRequestLiveData.postValue(null)
            }
        }
        val res = loginRepo.login(username, password, smsCodeHandler)
        if(!res) throw Exception("login failed")
    }

    // 登出
    fun logout() = withScope {
        loginRepo.logout()
        postLoginStateLiveData.postValue(null)
    }
}