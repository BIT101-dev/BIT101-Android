package cn.bit101.android.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.repo.base.LoginRepo
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.withScope
import cn.bit101.android.ui.common.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginOrLogoutViewModel @Inject constructor(
    private val loginStatusManager: LoginStatusManager,
    private val loginRepo: LoginRepo
) : ViewModel() {
    // 过程的状态
    val checkLoginStateLiveData = MutableLiveData<SimpleState?>(null)
    val postLoginStateLiveData = MutableLiveData<SimpleState?>(null)

    val sidFlow = loginStatusManager.sid.flow

    // 检查登录状态
    fun checkLoginState() = withSimpleStateLiveData(checkLoginStateLiveData) {
        val res = loginRepo.checkLogin()
        if(!res) throw Exception("check login error")
    }

    // 登录
    fun login(username: String, password: String) = withSimpleStateLiveData(postLoginStateLiveData) {
        val res = loginRepo.login(username, password)
        if(!res) throw Exception("login failed")
    }

    // 登出
    fun logout() = withScope {
        loginRepo.logout()
        postLoginStateLiveData.postValue(null)
    }
}