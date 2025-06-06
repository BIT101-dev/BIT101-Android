package cn.bit101.android.features.message

import androidx.lifecycle.ViewModel
import cn.bit101.android.data.repo.base.MessageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MessageViewModel @Inject constructor(
    private val messageRepo: MessageRepo,
) : ViewModel() {
}