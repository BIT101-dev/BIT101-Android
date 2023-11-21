package cn.bit101.android.ui.mine

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.App
import cn.bit101.android.BuildConfig
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.api.model.http.app.GetVersionDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UpdateUserInfoState {
    object Loading : UpdateUserInfoState
    object Fail : UpdateUserInfoState

    data class Success(
        val user: GetUserInfoDataModel.Response
    ) : UpdateUserInfoState
}


@HiltViewModel
class MineViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
) : ViewModel() {
    val updateUserInfoStateLiveData = MutableLiveData<UpdateUserInfoState>(null)

    // 更新用户信息
    fun updateUserInfo() {
        updateUserInfoStateLiveData.value = UpdateUserInfoState.Loading
        viewModelScope.launch {
            try {
                val res = BIT101API.user.getUserInfo("0").body() ?: throw Exception("getUserInfo error")
                updateUserInfoStateLiveData.postValue(UpdateUserInfoState.Success(res))
            } catch (e: Exception) {
                updateUserInfoStateLiveData.postValue(UpdateUserInfoState.Fail)
                e.printStackTrace()
            }
        }
    }
}