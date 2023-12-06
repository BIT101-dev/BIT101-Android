package cn.bit101.android.ui.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 暴露出来的状态
 */
data class RefreshAndLoadMoreStatesCombinedFlows <T>(
    val refreshStateFlow: StateFlow<SimpleState?>,
    val loadMoreStateFlow: StateFlow<SimpleState?>,
    val dataFlow: StateFlow<List<T>>,
    val pageFlow: StateFlow<Int>,
)

/**
 * 将*刷新*和*加载更多*的状态组合在一起
 */
class RefreshAndLoadMoreStatesCombined <T>(
    private val viewModelScope: CoroutineScope,
) {
    private val refreshStateFlow = MutableStateFlow<SimpleState?>(null)
    private val loadMoreStateFlow = MutableStateFlow<SimpleState?>(null)
    private val dataFlow = MutableStateFlow<List<T>>(emptyList())
    private val pageFlow = MutableStateFlow(0)

    var data: List<T>
        get() = dataFlow.value
        set(value) { dataFlow.value = value }

    /**
     * 将所有的状态暴露出来给组合函数
     */
    fun flows() = RefreshAndLoadMoreStatesCombinedFlows(
        refreshStateFlow = refreshStateFlow.asStateFlow(),
        loadMoreStateFlow = loadMoreStateFlow.asStateFlow(),
        dataFlow = dataFlow.asStateFlow(),
        pageFlow = pageFlow.asStateFlow(),
    )

    fun refresh(
        refresh: suspend () -> List<T>
    ) {
        refreshStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pageFlow.value = 0
                val posters = refresh()
                dataFlow.value = posters.toMutableList()
                refreshStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                refreshStateFlow.value = SimpleState.Fail
            }
        }
    }

    fun loadMore(
        loadMore: suspend (Long) -> List<T>
    ) {
        loadMoreStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(pageFlow.value >= 0) {
                    ++pageFlow.value
                    val posters = loadMore(pageFlow.value.toLong())
                    if (posters.isEmpty()) {
                        // 停止继续加载
                        pageFlow.value = -1
                    }
                    val allPosters = dataFlow.value.plus(posters)
                    dataFlow.value = allPosters
                }
                loadMoreStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                loadMoreStateFlow.value = SimpleState.Fail
            }
        }
    }
}