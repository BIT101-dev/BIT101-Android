package cn.bit101.android.ui.gallery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.MessageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val messageRepo: MessageRepo,
) : ViewModel() {

    val unreadMessageCountLiveData = MutableLiveData<Int>(null)

    fun getUnreadMessageCount() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val num = messageRepo.getUnreadMessageCount()
                unreadMessageCountLiveData.postValue(num)
            } catch (e: Exception) {
                e.printStackTrace()
                unreadMessageCountLiveData.postValue(0)
            }
        }
    }

}