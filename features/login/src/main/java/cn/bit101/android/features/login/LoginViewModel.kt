package cn.bit101.android.features.login

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.data.repo.base.CommunityLoginRepo
import cn.bit101.android.data.repo.base.LoginException
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.withSimpleErrorDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    status: LoginStatus,
    private val school: SchoolLoginRepo,
    private val community: CommunityLoginRepo,
    private val smsCodeRequestHub: SmsCodeRequestHub,
) : ViewModel() {
    val sid = status.sid.flow
    private val _loginState = MutableStateFlow<SimpleErrorDataState<String>?>(null)
    val loginState = _loginState.asStateFlow()
    private val _schoolReady = MutableStateFlow(false)
    val schoolReady = _schoolReady.asStateFlow()
    private var loginJob: Job? = null

    fun login(username: String, password: String, communityOnly: Boolean, schoolIdentity: Boolean = false) {
        loginJob = withSimpleErrorDataState(_loginState) {
            _schoolReady.value = false
            if (communityOnly) {
                if (schoolIdentity) community.loginWithSchool(username, password)
                else community.login(username, password)
                "BIT101 登录成功"
            } else {
                check(school.login(username, password, smsCodeRequestHub.createSmsCodeHandler()))
                _schoolReady.value = true
                try {
                    community.loginWithSchool(username, password, force = false)
                    "校园账号和 BIT101 登录成功"
                } catch (e: LoginException) {
                    "校园登录成功。BIT101 暂未连接，可稍后手动登录。"
                }
            }
        } ?: loginJob
    }

    fun enterCampus(): Boolean {
        if (!_schoolReady.value || _loginState.value !is SimpleErrorDataState.Loading) return false
        loginJob?.cancel()
        _loginState.value = null
        return true
    }

    fun clearResult() {
        if (_loginState.value !is SimpleErrorDataState.Loading) _loginState.value = null
    }
}
