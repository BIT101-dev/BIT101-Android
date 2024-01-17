package cn.bit101.android.ui.message

import androidx.lifecycle.ViewModel
import cn.bit101.android.repo.base.MessageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepo: MessageRepo,
) : ViewModel() {
}