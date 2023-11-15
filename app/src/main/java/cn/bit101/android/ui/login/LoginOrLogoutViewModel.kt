package cn.bit101.android.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.datastore.UserDataStore
import cn.bit101.android.status.DefaultLoginStatusManager
import cn.bit101.android.status.base.LoginStatusManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CheckLoginStateState {
    object Fail: CheckLoginStateState
    object Checking: CheckLoginStateState

    object Success: CheckLoginStateState
}

sealed interface PostLoginState {
    object Fail: PostLoginState
    object Loading: PostLoginState
    object Success : PostLoginState
}

@HiltViewModel
class LoginOrLogoutViewModel @Inject constructor(
    private val loginStatusManager: LoginStatusManager,
) : ViewModel() {
    // 过程的状态
    val checkLoginStateLiveData = MutableLiveData<CheckLoginStateState>(null)
    val postLoginStateLiveData = MutableLiveData<PostLoginState>(null)

    val sidFlow = UserDataStore.loginSid.flow

    // 检查登录状态
    fun checkLoginState() {
        Log.i("UserViewModel", checkLoginStateLiveData.value.toString())
        checkLoginStateLiveData.value = CheckLoginStateState.Checking
        viewModelScope.launch {
            try {
                val status = loginStatusManager.checkLogin()
                if(!status) throw Exception("check login error")
                checkLoginStateLiveData.postValue(CheckLoginStateState.Success)
            } catch (e: Exception) {
                checkLoginStateLiveData.postValue(CheckLoginStateState.Fail)
            }
        }
    }

    // 登录
    fun login(username: String, password: String) {
        postLoginStateLiveData.value = PostLoginState.Loading
        viewModelScope.launch {
            try {
                val success = loginStatusManager.login(username, password)
                if(!success) throw Exception("login failed")
                postLoginStateLiveData.postValue(PostLoginState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                postLoginStateLiveData.postValue(PostLoginState.Fail)
            }
        }
    }

    // 登出
    fun logout() {
        viewModelScope.launch {
            loginStatusManager.logout()
            postLoginStateLiveData.value = null
        }
    }

}