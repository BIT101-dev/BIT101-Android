package cn.bit101.android.features.login

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleErrorDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
internal class LoginOrLogoutViewModel @Inject constructor(
    private val loginStatus: LoginStatus,
    private val loginRepo: LoginRepo,
    private val smsCodeRequestHub: SmsCodeRequestHub,
) : ViewModel() {
    private val _checkLoginState = MutableStateFlow<SimpleErrorDataState<Boolean>?>(null)
    val checkLoginState = _checkLoginState.asStateFlow()
    private val _loginState = MutableStateFlow<SimpleErrorDataState<Unit>?>(null)
    val loginState = _loginState.asStateFlow()

    val sidFlow = loginStatus.sid.flow

    // 检查登录状态
    fun checkLoginState() {
        if (_checkLoginState.value != null) return
        withSimpleErrorDataState(_checkLoginState, cancelledState = SimpleErrorDataState.Success(false)) {
            loginRepo.checkLogin()
        }
    }

    // 登录
    fun login(username: String, password: String) {
        if (_checkLoginState.value is SimpleErrorDataState.Loading) return
        withSimpleErrorDataState(_loginState) {
            check(loginRepo.login(username, password, smsCodeRequestHub.createSmsCodeHandler()))
        }
    }

    // 登出
    fun logout() = withScope {
        loginRepo.logout()
        _loginState.value = null
        _checkLoginState.value = SimpleErrorDataState.Success(false)
    }
}
