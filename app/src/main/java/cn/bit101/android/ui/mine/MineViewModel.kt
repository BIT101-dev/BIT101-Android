package cn.bit101.android.ui.mine

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MineViewModel @Inject constructor(
    private val versionRepo: VersionRepo,
    private val userRepo: UserRepo,
) : ViewModel() {
    val userInfoStateLiveData = MutableLiveData<SimpleDataState<GetUserInfoDataModel.Response>>(null)

    private val _followingState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh { userRepo.getFollowings() }
        override fun loadMore() = loadMore { userRepo.getFollowings(it.toInt()) }
    }
    val followingStateExports = _followingState.export()

    private val _followerState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh { userRepo.getFollowers() }
        override fun loadMore() = loadMore { userRepo.getFollowers(it.toInt()) }
    }
    val followerStateExports = _followerState.export()

    // 更新用户信息
    fun updateUserInfo() {
        userInfoStateLiveData.value = SimpleDataState.Loading()
        viewModelScope.launch {
            try {
                val res = userRepo.getUserInfo(0)
                userInfoStateLiveData.postValue(SimpleDataState.Success(res))
            } catch (e: Exception) {
                userInfoStateLiveData.postValue(SimpleDataState.Fail())
                e.printStackTrace()
            }
        }
    }
}