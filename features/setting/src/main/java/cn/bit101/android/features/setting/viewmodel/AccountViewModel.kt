package cn.bit101.android.features.setting.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.data.repo.base.CommunityLoginRepo
import cn.bit101.android.data.repo.base.UploadRepo
import cn.bit101.android.data.repo.base.UserRepo
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.withSimpleErrorDataState
import cn.bit101.android.features.common.helper.successMessage
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withSimpleDataStateFlow
import cn.bit101.android.features.common.helper.withSimpleStateFlow
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AccountViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val communityLogin: CommunityLoginRepo,
    private val loginRepo: SchoolLoginRepo,
    private val uploadRepo: UploadRepo,
    private val loginStatus: LoginStatus
) : ViewModel() {
    private val _getUserInfoStateFlow = MutableStateFlow<SimpleDataState<GetUserInfoDataModel.Response>?>(null)
    val getUserInfoStateFlow = _getUserInfoStateFlow

    private val _reconnectSchoolStateFlow = MutableStateFlow<SimpleErrorDataState<String>?>(null)
    val reconnectSchoolStateFlow = _reconnectSchoolStateFlow

    val schoolConnected = loginStatus.schoolConnected.flow
    val communityState = communityLogin.state
    fun retryCommunity() { viewModelScope.launch { communityLogin.ensureConnected(force = true) } }
    fun clearUserInfo() { _getUserInfoStateFlow.value = null }

    val sidFlow = loginStatus.sid.flow

    private val _updateUserInfoStateFlow = MutableStateFlow<SimpleState?>(null)
    val updateUserInfoStateFlow = _updateUserInfoStateFlow

    private val _updateAvatarStateFlow = MutableStateFlow<SimpleState?>(null)
    val updateAvatarStateFlow = _updateAvatarStateFlow

    fun clearStates() {
        _getUserInfoStateFlow.value = null
        _reconnectSchoolStateFlow.value = null
        _updateUserInfoStateFlow.value = null
        _updateAvatarStateFlow.value = null
    }

    fun getUserInfo() = withSimpleDataStateFlow(_getUserInfoStateFlow) {
        userRepo.getUserInfo(0)
    }

    fun reconnectSchool() = withSimpleErrorDataState(_reconnectSchoolStateFlow) {
        loginRepo.reconnect().successMessage()
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            loginRepo.logout()
        }
    }

    fun updateAvatar(uri: Uri) = withSimpleStateFlow(_updateAvatarStateFlow) {
        val avatar = uploadRepo.uploadImage(uri)
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