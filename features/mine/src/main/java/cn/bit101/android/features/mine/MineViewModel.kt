package cn.bit101.android.features.mine

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.base.MessageRepo
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.data.repo.base.UserRepo
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.withSimpleDataStateLiveData
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MineViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val posterRepo: PosterRepo,
    private val messageRepo: MessageRepo
) : ViewModel() {
    val userInfoStateLiveData = MutableLiveData<SimpleDataState<GetUserInfoDataModel.Response>?>(null)

    val messageCountStateLiveData = MutableLiveData<SimpleDataState<Int>?>(null)

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

    private val _postersState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh { posterRepo.getPostersOfUserByUid(0) }
        override fun loadMore() = loadMore { posterRepo.getPostersOfUserByUid(0, it) }
    }
    val postersStateExports = _postersState.export()

    // 更新用户信息
    fun updateUserInfo() = withSimpleDataStateLiveData(userInfoStateLiveData) {
        userRepo.getUserInfo(0)
    }

    fun updateMessageCount() = withSimpleDataStateLiveData(messageCountStateLiveData) {
        messageRepo.getUnreadMessageCount()
    }
}