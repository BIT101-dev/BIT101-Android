package cn.bit101.android.features.mine

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.GallerySettings
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
    private val messageRepo: MessageRepo,
    gallerySettings: GallerySettings,
) : ViewModel() {
    val userInfoStateLiveData = MutableLiveData<SimpleDataState<GetUserInfoDataModel.Response>?>(null)

    val messageCountStateLiveData = MutableLiveData<SimpleDataState<Int>?>(null)

    private val newLoadMode = gallerySettings.hideBotPoster.flow

    private val _followingState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh(
            newLoadMode,
            refresh = { userRepo.getFollowings() },
            loadMore = { userRepo.getFollowings(it.toInt()) }
        )
        override fun loadMore() = loadMore(newLoadMode) { userRepo.getFollowings(it.toInt()) }
    }
    val followingStateExports = _followingState.export()

    private val _followerState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh(
            newLoadMode,
            refresh = { userRepo.getFollowers() },
            loadMore = { userRepo.getFollowers(it.toInt()) }
        )
        override fun loadMore() = loadMore(newLoadMode) { userRepo.getFollowers(it.toInt()) }
    }
    val followerStateExports = _followerState.export()

    private val _postersState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh(
            newLoadMode,
            refresh = { posterRepo.getPostersOfUserByUid(0) },
            loadMore = { posterRepo.getPostersOfUserByUid(0, it) }
        )
        override fun loadMore() = loadMore(newLoadMode) { posterRepo.getPostersOfUserByUid(0, it) }
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