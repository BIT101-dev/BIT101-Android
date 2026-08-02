package cn.bit101.android.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.base.MessageRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withSimpleDataStateFlow
import cn.bit101.api.model.common.MessageType
import cn.bit101.api.model.http.bit101.GetMessagesDataModel
import cn.bit101.api.model.http.bit101.GetSeparateMessagesNumberDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MessageViewModel @Inject constructor(
    private val messageRepo: MessageRepo,
) : ViewModel() {

    private val _unreadCountStateFlow = MutableStateFlow<SimpleDataState<Int>?>(null)
    val unreadCountStateFlow = _unreadCountStateFlow.asStateFlow()

    private val _separateUnreadCountStateFlow =
        MutableStateFlow<SimpleDataState<GetSeparateMessagesNumberDataModel.Response>?>(null)
    val separateUnreadCountStateFlow = _separateUnreadCountStateFlow.asStateFlow()

    private val _selectedTypeFlow = MutableStateFlow(MessageType.SYSTEM)
    val selectedTypeFlow = _selectedTypeFlow.asStateFlow()

    // 每个类型的消息缓存，切换 tab 时直接展示缓存，不重新请求
    private val _messagesStateByTypeFlow =
        MutableStateFlow<Map<String, SimpleDataState<List<GetMessagesDataModel.ResponseItem>>>>(emptyMap())
    val messagesStateByTypeFlow = _messagesStateByTypeFlow.asStateFlow()

    // 每个类型的加载更多状态
    private val _loadMoreStateByTypeFlow = MutableStateFlow<Map<String, SimpleState>>(emptyMap())
    val loadMoreStateByTypeFlow = _loadMoreStateByTypeFlow.asStateFlow()

    fun loadUnreadCounts() {
        withSimpleDataStateFlow(_unreadCountStateFlow) {
            messageRepo.getUnreadMessageCount()
        }
        withSimpleDataStateFlow(_separateUnreadCountStateFlow) {
            messageRepo.getSeparateUnreadMessageCount()
        }
    }

    fun selectType(type: String) {
        if (_selectedTypeFlow.value == type) return
        _selectedTypeFlow.value = type
        // 已缓存则直接展示，避免重新加载
        if (!_messagesStateByTypeFlow.value.containsKey(type)) {
            refreshMessages()
        }
    }

    fun refreshMessages() {
        val type = _selectedTypeFlow.value
        viewModelScope.launch(Dispatchers.IO) {
            _messagesStateByTypeFlow.value += (type to SimpleDataState.Loading<List<GetMessagesDataModel.ResponseItem>>())
            runCatching {
                val res = messageRepo.getMessages(type = type, lastID = null)
                _messagesStateByTypeFlow.value += (type to SimpleDataState.Success(res))
                // 获取消息后服务端会清空该类型未读数，同步更新未读数
                reloadUnreadCounts()
            }.onFailure {
                it.printStackTrace()
                _messagesStateByTypeFlow.value += (type to SimpleDataState.Fail<List<GetMessagesDataModel.ResponseItem>>())
            }
        }
    }

    fun loadMoreMessages() {
        val type = _selectedTypeFlow.value
        val state = _messagesStateByTypeFlow.value[type] as? SimpleDataState.Success ?: return
        val lastID = state.data.lastOrNull()?.id ?: return
        if (_loadMoreStateByTypeFlow.value[type] is SimpleState.Loading) return
        viewModelScope.launch(Dispatchers.IO) {
            _loadMoreStateByTypeFlow.value += (type to SimpleState.Loading)
            runCatching {
                val more = messageRepo.getMessages(type = type, lastID = lastID)
                _messagesStateByTypeFlow.value += (type to SimpleDataState.Success(state.data + more))
                _loadMoreStateByTypeFlow.value += (type to SimpleState.Success)
            }.onFailure {
                it.printStackTrace()
                _loadMoreStateByTypeFlow.value += (type to SimpleState.Fail)
            }
        }
    }

    private fun reloadUnreadCounts() = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            _unreadCountStateFlow.value = SimpleDataState.Success(messageRepo.getUnreadMessageCount())
            _separateUnreadCountStateFlow.value =
                SimpleDataState.Success(messageRepo.getSeparateUnreadMessageCount())
        }
    }
}
