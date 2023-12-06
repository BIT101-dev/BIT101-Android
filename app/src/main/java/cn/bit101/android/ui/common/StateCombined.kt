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
interface BasicRefreshAndLoadMoreStatesCombinedExportData <T>{
    val refreshStateFlow: StateFlow<SimpleState?>
    val loadMoreStateFlow: StateFlow<SimpleState?>
    val dataFlow: StateFlow<List<T>>
    val pageFlow: StateFlow<Int>
}

data class RefreshAndLoadMoreStatesCombinedExportDataOne <A, T>(
    override val refreshStateFlow: StateFlow<SimpleState?>,
    override val loadMoreStateFlow: StateFlow<SimpleState?>,
    override val dataFlow: StateFlow<List<T>>,
    override val pageFlow: StateFlow<Int>,

    val refresh: (A) -> Unit,
    val loadMore: (A) -> Unit,
) : BasicRefreshAndLoadMoreStatesCombinedExportData<T>

data class RefreshAndLoadMoreStatesCombinedExportDataZero <T>(
    override val refreshStateFlow: StateFlow<SimpleState?>,
    override val loadMoreStateFlow: StateFlow<SimpleState?>,
    override val dataFlow: StateFlow<List<T>>,
    override val pageFlow: StateFlow<Int>,

    val refresh: () -> Unit,
    val loadMore: () -> Unit,
) : BasicRefreshAndLoadMoreStatesCombinedExportData<T>


/**
 * 将*刷新*和*加载更多*的状态组合在一起
 */
abstract class BasicRefreshAndLoadMoreStatesCombined <T>(
    private val viewModelScope: CoroutineScope,
) {
    protected val refreshStateFlow = MutableStateFlow<SimpleState?>(null)
    protected val loadMoreStateFlow = MutableStateFlow<SimpleState?>(null)
    protected val dataFlow = MutableStateFlow<List<T>>(emptyList())
    protected val pageFlow = MutableStateFlow(0)

    var data: List<T>
        get() = dataFlow.value
        set(value) { dataFlow.value = value }

    protected fun refresh(
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

    protected fun loadMore(
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

/**
 * 将*刷新*和*加载更多*的状态组合在一起，增加了一个参数
 */
abstract class RefreshAndLoadMoreStatesCombinedOne <A, T>(
    viewModelScope: CoroutineScope,
) : BasicRefreshAndLoadMoreStatesCombined<T>(viewModelScope) {

    /**
     * 将所有的状态暴露出来给组合函数
     */
    fun export() = RefreshAndLoadMoreStatesCombinedExportDataOne <A, T>(
        refreshStateFlow = refreshStateFlow.asStateFlow(),
        loadMoreStateFlow = loadMoreStateFlow.asStateFlow(),
        dataFlow = dataFlow.asStateFlow(),
        pageFlow = pageFlow.asStateFlow(),

        refresh = { refresh(it) },
        loadMore = { loadMore(it) },
    )

    abstract fun refresh(data: A)
    abstract fun loadMore(data: A)
}

/**
 * 将*刷新*和*加载更多*的状态组合在一起
 */
abstract class RefreshAndLoadMoreStatesCombinedZero <T>(
    viewModelScope: CoroutineScope,
) : BasicRefreshAndLoadMoreStatesCombined<T>(viewModelScope) {

    /**
     * 将所有的状态暴露出来给组合函数
     */
    fun export() = RefreshAndLoadMoreStatesCombinedExportDataZero <T>(
        refreshStateFlow = refreshStateFlow.asStateFlow(),
        loadMoreStateFlow = loadMoreStateFlow.asStateFlow(),
        dataFlow = dataFlow.asStateFlow(),
        pageFlow = pageFlow.asStateFlow(),

        refresh = { refresh() },
        loadMore = { loadMore() },
    )

    abstract fun refresh()
    abstract fun loadMore()
}