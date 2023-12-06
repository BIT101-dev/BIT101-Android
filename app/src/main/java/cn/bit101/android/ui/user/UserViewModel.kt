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
    private val uploadRepo: UploadRepo,
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

    private val _followersState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh {
            userRepo.getFollowers()
        }

        override fun loadMore() = loadMore { page ->
            userRepo.getFollowers(page.toInt())
        }

    }
    val followersStateExport = _followersState.export()

    private val _followingsState = object : RefreshAndLoadMoreStatesCombinedZero<User>(viewModelScope) {
        override fun refresh() = refresh {
            userRepo.getFollowings()
        }

        override fun loadMore() = loadMore {
            userRepo.getFollowings(it.toInt())
        }

    }
    val followingsStateExport = _followingsState.export()

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