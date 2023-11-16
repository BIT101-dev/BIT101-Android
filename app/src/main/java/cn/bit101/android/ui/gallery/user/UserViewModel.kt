package cn.bit101.android.ui.gallery.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _getUserInfoStateFlow = MutableStateFlow<SimpleDataState<GetUserInfoDataModel.Response>?>(null)
    val getUserInfoStateFlow = _getUserInfoStateFlow


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

}