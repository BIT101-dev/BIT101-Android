package cn.bit101.android.ui.user

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.repo.base.UploadRepo
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombinedOne
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.android.ui.common.UploadImageState
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
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

    val followStateMutableLiveData = MutableLiveData<SimpleState>(null)

    val uploadUserInfoStateLiveData = MutableLiveData<SimpleState>(null)

    fun getUserInfo(id: Long) {
        _getUserInfoStateFlow.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                userRepo.getUserInfo(id).let {
                    _getUserInfoStateFlow.value = SimpleDataState.Success(it)
                }
            }.onFailure {
                _getUserInfoStateFlow.value = SimpleDataState.Fail()
            }
        }
    }

    fun follow(uid: Long) {
        followStateMutableLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val res = userRepo.follow(uid)
                val data = (_getUserInfoStateFlow.value as SimpleDataState.Success).data
                _getUserInfoStateFlow.value = SimpleDataState.Success(data.copy(
                    follower = res.follower,
                    followerNum = res.followerNum,
                    following = res.following,
                    followingNum = res.followingNum
                ))
                followStateMutableLiveData.postValue(SimpleState.Success)
            }.onFailure {
                followStateMutableLiveData.postValue(SimpleState.Fail)
            }
        }
    }
}