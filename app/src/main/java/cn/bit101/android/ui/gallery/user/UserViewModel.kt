package cn.bit101.android.ui.gallery.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import cn.bit101.android.ui.gallery.common.StateCombinedUseFlow
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val posterRepo: PosterRepo,
) : ViewModel() {

    private val _getUserInfoStateFlow = MutableStateFlow<SimpleDataState<GetUserInfoDataModel.Response>?>(null)
    val getUserInfoStateFlow = _getUserInfoStateFlow

    val posterState = StateCombinedUseFlow<GetPostersDataModel.ResponseItem>(viewModelScope)

    val followStateMutableLiveData = MutableLiveData<SimpleState>(null)


    fun getUserInfo(id: Long) {
        _getUserInfoStateFlow.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                userRepo.getUserInfo(id).let {
                    _getUserInfoStateFlow.value = SimpleDataState.Success(it)
                }
            }.onFailure {
                _getUserInfoStateFlow.value = SimpleDataState.Error()
            }
        }
    }

    fun loadMorePosters(uid: Long) = posterState.loadMore { page ->
        posterRepo.getPostersOfUserByUid(uid, page)
    }

    fun refreshPoster(uid: Long) = posterState.refresh {
        posterRepo.getPostersOfUserByUid(uid)
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
                followStateMutableLiveData.postValue(SimpleState.Error)
            }
        }
    }

}