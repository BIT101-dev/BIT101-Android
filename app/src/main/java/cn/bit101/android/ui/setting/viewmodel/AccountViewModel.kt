package cn.bit101.android.ui.setting.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.repo.base.LoginRepo
import cn.bit101.android.repo.base.UploadRepo
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.withSimpleDataStateFlow
import cn.bit101.android.ui.common.withSimpleStateFlow
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
    private val uploadRepo: UploadRepo,
    private val loginStatusManager: LoginStatusManager
) : ViewModel() {
    private val _getUserInfoStateFlow = MutableStateFlow<SimpleDataState<GetUserInfoDataModel.Response>?>(null)
    val getUserInfoStateFlow = _getUserInfoStateFlow

    private val _checkLoginStateFlow = MutableStateFlow<SimpleState?>(null)
    val checkLoginStateFlow = _checkLoginStateFlow

    val loginStatusFlow = loginStatusManager.status.flow

    val sidFlow = loginStatusManager.sid.flow

    private val _updateUserInfoStateFlow = MutableStateFlow<SimpleState?>(null)
    val updateUserInfoStateFlow = _updateUserInfoStateFlow

    private val _updateAvatarStateFlow = MutableStateFlow<SimpleState?>(null)
    val updateAvatarStateFlow = _updateAvatarStateFlow

    fun clearStates() {
        _getUserInfoStateFlow.value = null
        _checkLoginStateFlow.value = null
        _updateUserInfoStateFlow.value = null
        _updateAvatarStateFlow.value = null
    }

    fun getUserInfo() = withSimpleDataStateFlow(_getUserInfoStateFlow) {
        userRepo.getUserInfo(0)
    }

    fun checkLogin() = withSimpleStateFlow(_checkLoginStateFlow) {
        loginRepo.checkLogin()
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            loginRepo.logout()
        }
    }

    fun updateAvatar(context: Context, uri: Uri) = withSimpleStateFlow(_updateAvatarStateFlow) {
        val avatar = uploadRepo.uploadImage(context, uri)
        val oldData = (getUserInfoStateFlow.value as SimpleDataState.Success).data
        userRepo.updateUser(
            avatarMid = avatar.mid,
            nickname = oldData.user.nickname,
            motto = oldData.user.motto
        )
        _getUserInfoStateFlow.value = SimpleDataState.Success(
            oldData.copy(
                user = oldData.user.copy(avatar = avatar)
            )
        )
    }

    fun updateUserInfo(
        nickname: String? = null,
        motto: String? = null
    ) = withSimpleStateFlow(_updateUserInfoStateFlow) {
        val oldData = (getUserInfoStateFlow.value as SimpleDataState.Success).data

        val finalNickname = nickname ?: oldData.user.nickname
        val finalMotto = motto ?: oldData.user.motto

        userRepo.updateUser(
            avatarMid = oldData.user.avatar.mid,
            nickname = finalNickname,
            motto = finalMotto
        )

        _getUserInfoStateFlow.value = SimpleDataState.Success(
            oldData.copy(
                user = oldData.user.copy(
                    nickname = finalNickname,
                    motto = finalMotto
                )
            )
        )
    }
}