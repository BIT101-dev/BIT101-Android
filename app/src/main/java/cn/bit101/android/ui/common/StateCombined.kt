package cn.bit101.android.ui.gallery.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 刷新状态
 */
sealed interface RefreshState {
    object Loading : RefreshState
    object Fail : RefreshState
    object Success : RefreshState
}

/**
 * 加载更多状态
 */
sealed interface LoadMoreState {
    object Loading : LoadMoreState
    object Fail : LoadMoreState
    object Success : LoadMoreState
}

/**
 * 将*刷新*和*加载更多*的状态组合在一起
 */
class RefreshAndLoadMoreStatesCombined <T>(
    private val viewModelScope: CoroutineScope,
) {
    private val _refreshStateFlow = MutableStateFlow<RefreshState?>(null)
    val refreshStateFlow = _refreshStateFlow.asStateFlow()

    private val _loadMoreStateFlow = MutableStateFlow<LoadMoreState?>(null)
    val loadMoreStateFlow = _loadMoreStateFlow.asStateFlow()

    private val _dataFlow = MutableStateFlow<List<T>>(emptyList())
    val dataFlow = _dataFlow.asStateFlow()

    private var page = 0

    fun refresh(
        getPosters: suspend () -> List<T>
    ) {
        _refreshStateFlow.value = RefreshState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                page = 0
                val posters = getPosters()
                _dataFlow.value = posters.toMutableList()
                _refreshStateFlow.value = RefreshState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _refreshStateFlow.value = RefreshState.Fail
            }
        }
    }

    fun loadMore(
        loadPosters: suspend (Long) -> List<T>
    ) {
        _loadMoreStateFlow.value = LoadMoreState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(page >= 0) {
                    ++page
                    val posters = loadPosters(page.toLong())
                    if (posters.isEmpty()) {
                        // 停止继续加载
                        page = -1
                    }
                    val allPosters = dataFlow.value.plus(posters)
                    _dataFlow.value = allPosters
                }
                _loadMoreStateFlow.value = LoadMoreState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _loadMoreStateFlow.value = LoadMoreState.Fail
            }
        }
    }

    fun setData(data: List<T>) {
        _dataFlow.value = data
    }
}