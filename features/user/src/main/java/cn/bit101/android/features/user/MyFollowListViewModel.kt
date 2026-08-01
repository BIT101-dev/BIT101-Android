package cn.bit101.android.features.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.data.repo.base.UserRepo
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MyFollowListViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val posterRepo: PosterRepo,
) : ViewModel() {

    private val _followingState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh { userRepo.getFollowings() }
        override fun loadMore() = loadMore { userRepo.getFollowings(it.toInt()) }
    }
    val followingStateExport = _followingState.export()

    private val _followerState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh { userRepo.getFollowers() }
        override fun loadMore() = loadMore { userRepo.getFollowers(it.toInt()) }
    }
    val followerStateExport = _followerState.export()

    private val _postersState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh { posterRepo.getPostersOfUserByUid(0) }
        override fun loadMore() = loadMore { posterRepo.getPostersOfUserByUid(0, it) }
    }
    val postersStateExport = _postersState.export()
}
