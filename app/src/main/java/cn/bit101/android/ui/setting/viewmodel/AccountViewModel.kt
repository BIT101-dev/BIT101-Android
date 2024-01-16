package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.repo.base.LoginRepo
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val loginRepo: LoginRepo,
    private val loginStatusManager: LoginStatusManager
) : ViewModel() {
    private val _getUserInfoStateFlow = MutableStateFlow<SimpleDataState<GetUserInfoDataModel.Response>?>(null)
    val getUserInfoStateFlow = _getUserInfoStateFlow

    private val _checkLoginStateFlow = MutableStateFlow<SimpleState?>(null)
    val checkLoginStateFlow = _checkLoginStateFlow

    val loginStatusFlow = loginStatusManager.status.flow

    val sidFlow = loginStatusManager.sid.flow

    fun clearStates() {
        _getUserInfoStateFlow.value = null
        _checkLoginStateFlow.value = null
    }

    fun getUserInfo() {
        _getUserInfoStateFlow.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                userRepo.getUserInfo(0).let {
                    _getUserInfoStateFlow.value = SimpleDataState.Success(it)
                }
            }.onFailure {
                _getUserInfoStateFlow.value = SimpleDataState.Fail()
            }
        }
    }

    fun checkLogin() {
        _checkLoginStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                loginRepo.checkLogin()
                _checkLoginStateFlow.value = SimpleState.Success
            }.onFailure {
                _checkLoginStateFlow.value = SimpleState.Fail
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            loginRepo.logout()
        }
    }
}