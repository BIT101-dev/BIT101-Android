package cn.bit101.android.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.datastore.UserDataStore
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.repo.base.LoginRepo
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
    private val loginRepo: LoginRepo
) : ViewModel() {
    // 过程的状态
    val checkLoginStateLiveData = MutableLiveData<CheckLoginStateState>(null)
    val postLoginStateLiveData = MutableLiveData<PostLoginState>(null)

    val sidFlow = loginStatusManager.sid.flow

    // 检查登录状态
    fun checkLoginState() {
        checkLoginStateLiveData.value = CheckLoginStateState.Checking
        viewModelScope.launch {
            try {
                val res = loginRepo.checkLogin()
                if(!res) throw Exception("check login error")
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
                val res = loginRepo.login(username, password)
                if(!res) throw Exception("login failed")
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
            loginRepo.logout()
            postLoginStateLiveData.value = null
        }
    }

}