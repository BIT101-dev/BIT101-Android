package cn.bit101.android.features.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.data.repo.base.UserRepo
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedOne
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withSimpleDataStateFlow
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
internal class UserViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val posterRepo: PosterRepo,
) : ViewModel() {

    private val _getUserInfoStateFlow = MutableStateFlow<SimpleDataState<GetUserInfoDataModel.Response>?>(null)
    val getUserInfoStateFlow = _getUserInfoStateFlow

    private val _posterState = object : RefreshAndLoadMoreStatesCombinedOne<Long, GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh(data: Long) = refresh {
            posterRepo.getPostersOfUserByUid(data)
        }

        override fun loadMore(data: Long) = loadMore { page ->
            posterRepo.getPostersOfUserByUid(data, page)
        }
    }
    val posterStateExport = _posterState.export()

    val followStateMutableLiveData = MutableLiveData<SimpleState?>(null)

    val uploadUserInfoStateLiveData = MutableLiveData<SimpleState?>(null)

    fun getUserInfo(id: Long) = withSimpleDataStateFlow(_getUserInfoStateFlow) {
        userRepo.getUserInfo(id)
    }

    fun follow(uid: Long) = withSimpleStateLiveData(followStateMutableLiveData) {
        val res = userRepo.follow(uid)
        val data = (_getUserInfoStateFlow.value as SimpleDataState.Success).data
        _getUserInfoStateFlow.value = SimpleDataState.Success(data.copy(
            follower = res.follower,
            followerNum = res.followerNum,
            following = res.following,
            followingNum = res.followingNum
        ))
    }
}