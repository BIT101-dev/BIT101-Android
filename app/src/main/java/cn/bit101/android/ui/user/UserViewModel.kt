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
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombined
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
    private val uploadRepo: UploadRepo,
) : ViewModel() {

    private val _getUserInfoStateFlow = MutableStateFlow<SimpleDataState<GetUserInfoDataModel.Response>?>(null)
    val getUserInfoStateFlow = _getUserInfoStateFlow

    private val _posterState = RefreshAndLoadMoreStatesCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val posterStateFlows = _posterState.flows()

    val followStateMutableLiveData = MutableLiveData<SimpleState>(null)

    private val _followersState = RefreshAndLoadMoreStatesCombined<User>(viewModelScope)
    val followersStateFlows = _followersState.flows()

    private val _followingsState = RefreshAndLoadMoreStatesCombined<User>(viewModelScope)
    val followingsStateFlows = _followingsState.flows()

    val uploadAvatarState = MutableLiveData<UploadImageState>(null)

    private val _editUserDataFlow = MutableStateFlow<User?>(null)
    val editUserDataFlow = _editUserDataFlow.asStateFlow()

    val uploadUserInfoStateLiveData = MutableLiveData<SimpleState>(null)

    fun getUserInfo(id: Long) {
        _getUserInfoStateFlow.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                userRepo.getUserInfo(id).let {
                    _getUserInfoStateFlow.value = SimpleDataState.Success(it)
                    _editUserDataFlow.value = it.user
                }
            }.onFailure {
                _getUserInfoStateFlow.value = SimpleDataState.Fail()
            }
        }
    }

    fun loadMorePosters(uid: Long) = _posterState.loadMore { page ->
        posterRepo.getPostersOfUserByUid(uid, page)
    }

    fun refreshPoster(uid: Long) = _posterState.refresh {
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
                followStateMutableLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    fun refreshFollowers() = _followersState.refresh {
        userRepo.getFollowers()
    }

    fun loadMoreFollowers() = _followersState.loadMore { page ->
        userRepo.getFollowers(page.toInt())
    }

    fun refreshFollowings() = _followingsState.refresh {
        userRepo.getFollowings()
    }

    fun loadMoreFollowings() = _followingsState.loadMore { page ->
        userRepo.getFollowings(page.toInt())
    }


    fun uploadAvatar(context: Context, uri: Uri) {
        uploadAvatarState.value = UploadImageState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = uploadRepo.uploadImage(context, uri)
                uploadAvatarState.postValue(UploadImageState.Success(image))

                val data = editUserDataFlow.value
                _editUserDataFlow.value = data!!.copy(avatar = image)

            } catch (e: Exception) {
                e.printStackTrace()
                uploadAvatarState.postValue(UploadImageState.Fail)
            }
        }
    }

    fun setUserEditData(user: User) {
        _editUserDataFlow.value = user
    }

    fun saveUserEditData() {
        uploadUserInfoStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = editUserDataFlow.value!!
                val userInfo = (getUserInfoStateFlow.value as SimpleDataState.Success).data

                userRepo.updateUser(
                    avatarMid = user.avatar.mid,
                    nickname = user.nickname,
                    motto = user.motto
                )

                _getUserInfoStateFlow.value = SimpleDataState.Success(userInfo.copy(
                    user = user
                ))

                uploadUserInfoStateLiveData.postValue(SimpleState.Success)
            }.onFailure {
                it.printStackTrace()
                uploadUserInfoStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }
}