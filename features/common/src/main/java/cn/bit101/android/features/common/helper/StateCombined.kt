package cn.bit101.android.features.common.helper

import cn.bit101.api.model.UniqueData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/**
 * 暴露出来的状态
 */
interface BasicRefreshAndLoadMoreStatesCombinedExportData <T : UniqueData>{
    val refreshStateFlow: StateFlow<SimpleState?>
    val loadMoreStateFlow: StateFlow<SimpleState?>
    val dataFlow: StateFlow<List<T>>
    val pageFlow: StateFlow<Int>

    val refresh: Function<Unit>
    val loadMore: Function<Unit>
}

interface BasicRefreshAndLoadMoreStatesCombinedExportDataOne <A, T : UniqueData>
    : BasicRefreshAndLoadMoreStatesCombinedExportData<T> {
    override val refresh: (A) -> Unit
    override val loadMore: (A) -> Unit
}

interface BasicRefreshAndLoadMoreStatesCombinedExportDataZero <T : UniqueData>
    : BasicRefreshAndLoadMoreStatesCombinedExportData<T> {
    override val refresh: () -> Unit
    override val loadMore: () -> Unit
}

data class RefreshAndLoadMoreStatesCombinedExportDataOne <A, T : UniqueData>(
    override val refreshStateFlow: StateFlow<SimpleState?>,
    override val loadMoreStateFlow: StateFlow<SimpleState?>,
    override val dataFlow: StateFlow<List<T>>,
    override val pageFlow: StateFlow<Int>,

    override val refresh: (A) -> Unit,
    override val loadMore: (A) -> Unit,
) : BasicRefreshAndLoadMoreStatesCombinedExportDataOne<A, T>

data class RefreshAndLoadMoreStatesCombinedExportDataZero <T : UniqueData>(
    override val refreshStateFlow: StateFlow<SimpleState?>,
    override val loadMoreStateFlow: StateFlow<SimpleState?>,
    override val dataFlow: StateFlow<List<T>>,
    override val pageFlow: StateFlow<Int>,

    override val refresh: () -> Unit,
    override val loadMore: () -> Unit,
) : BasicRefreshAndLoadMoreStatesCombinedExportDataZero<T>


/**
 * 将*刷新*和*加载更多*的状态组合在一起
 */
abstract class BasicRefreshAndLoadMoreStatesCombined <T : UniqueData>(
    private val viewModelScope: CoroutineScope,
) {
    protected val refreshStateFlow = MutableStateFlow<SimpleState?>(null)
    protected val loadMoreStateFlow = MutableStateFlow<SimpleState?>(null)
    protected val dataFlow = MutableStateFlow<List<T>>(emptyList())
    protected val pageFlow = MutableStateFlow(0)

    var data: List<T>
        get() = dataFlow.value
        set(value) { dataFlow.value = value }

    abstract fun export(): BasicRefreshAndLoadMoreStatesCombinedExportData<T>

    protected fun refresh(
        refresh: suspend () -> List<T>
    ) {
        if(refreshStateFlow.value == SimpleState.Loading) return
        refreshStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pageFlow.value = 0
                val posters = refresh()
                dataFlow.value = posters.toMutableList().distinctBy { it.id }
                if(posters.isEmpty()) {
                    pageFlow.value = -1
                }
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
        if(loadMoreStateFlow.value == SimpleState.Loading) return
        loadMoreStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            var page = pageFlow.value
            try {
                if(page >= 0) {
                    ++page
                    val posters = loadMore(page.toLong())
                    if (posters.isEmpty()) {
                        // 停止继续加载
                        page = -1
                    }
                    val allPosters = dataFlow.value.plus(posters).distinctBy { it.id }
                    dataFlow.value = allPosters
                }
                loadMoreStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                loadMoreStateFlow.value = SimpleState.Fail
            }
            pageFlow.value = page
        }
    }
}

/**
 * 将*刷新*和*加载更多*的状态组合在一起
 * 按输入进行过滤, 并自动重新请求, 以保证刷新和单次加载获取的元素数目在正常水平, 且不会意外提前终止
 * 允许传入一个 flow, 作为过滤器的参数
 */
class FilteredStateZero<T : UniqueData, U>(
    val viewModelScope: CoroutineScope,
    val posterGetter: suspend (Long?) -> List<T>,
    val filterer: (T, U) -> Boolean,
    val filterDataFlow: Flow<U>,
    val refreshThreshold: Int = 10,
    val loadMoreThreshold: Int = 3,
) : RefreshAndLoadMoreStatesCombinedZero<T>(
    viewModelScope
) {
    override fun refresh() {
        if (refreshStateFlow.value == SimpleState.Loading) return
        refreshStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filterData = filterDataFlow.first()
                pageFlow.value = 0
                var data = posterGetter(null).toMutableList().distinctBy { it.id }
                if (data.isEmpty()) {
                    pageFlow.value = -1
                } else {
                    data = data.filter { filterer(it, filterData) }
                    while (data.size < refreshThreshold) {
                        val newPosters = posterGetter(pageFlow.value.toLong())
                        if (newPosters.isEmpty()) {
                            pageFlow.value = -1
                            break
                        }
                        pageFlow.value++

                        data = data
                            .plus(newPosters.filter { filterer(it, filterData) })
                            .toMutableList()
                            .distinctBy { it.id }
                    }
                }
                dataFlow.value = data
                refreshStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                refreshStateFlow.value = SimpleState.Fail
            }
        }
    }

    override fun loadMore() {
        if (loadMoreStateFlow.value == SimpleState.Loading) return
        if (pageFlow.value == -1) {
            loadMoreStateFlow.value = SimpleState.Success
            return
        }
        loadMoreStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filterData = filterDataFlow.first()
                var finalData = dataFlow.value
                val initSize = finalData.size
                while (true) {
                    val newPosters = posterGetter(pageFlow.value.toLong())
                    if (newPosters.isEmpty()) {
                        pageFlow.value = -1
                        break
                    }
                    pageFlow.value++

                    finalData = finalData
                        .plus(newPosters.filter { filterer(it, filterData) })
                        .toMutableList()
                        .distinctBy { it.id }
                    if (finalData.size - initSize >= loadMoreThreshold) break
                }
                dataFlow.value = finalData
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
abstract class RefreshAndLoadMoreStatesCombinedOne <A, T : UniqueData>(
    viewModelScope: CoroutineScope,
) : BasicRefreshAndLoadMoreStatesCombined<T>(viewModelScope) {

    /**
     * 将所有的状态暴露出来给组合函数
     */
    override fun export() = RefreshAndLoadMoreStatesCombinedExportDataOne <A, T>(
        refreshStateFlow = refreshStateFlow.asStateFlow(),
        loadMoreStateFlow = loadMoreStateFlow.asStateFlow(),
        dataFlow = dataFlow.asStateFlow(),
        pageFlow = pageFlow.asStateFlow(),

        refresh = this::refresh,
        loadMore = this::loadMore,
    )

    abstract fun refresh(data: A)
    abstract fun loadMore(data: A)
}


/**
 * 将*刷新*和*加载更多*的状态组合在一起, 增加了一个参数, 并支持投影
 * 按输入进行过滤, 并自动重新请求, 以保证刷新和单次加载获取的元素数目在正常水平, 且不会意外提前终止
 * 允许传入一个 flow, 作为过滤器和投影的参数
 */
class FilteredStateOne<A, T : UniqueData, U>(
    val viewModelScope: CoroutineScope,
    val posterGetter: suspend (A, Long?) -> List<T>,
    val filterer: (T, U) -> Boolean,
    val filterDataFlow: Flow<U>,
    val projection: (T, U) -> T = { data, _ -> data },
    val refreshThreshold: Int = 10,
    val loadMoreThreshold: Int = 3,
) : RefreshAndLoadMoreStatesCombinedOne<A, T>(
    viewModelScope
) {
    override fun refresh(data: A) {
        if (refreshStateFlow.value == SimpleState.Loading) return
        refreshStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filterData = filterDataFlow.first()
                pageFlow.value = 0
                var dat = posterGetter(data, null).toMutableList().distinctBy { it.id }
                if (dat.isEmpty()) {
                    pageFlow.value = -1
                } else {
                    dat = dat.filter { filterer(it, filterData) }.map { projection(it, filterData) }
                    while (dat.size < refreshThreshold) {
                        val newPosters = posterGetter(data, pageFlow.value.toLong())
                        if (newPosters.isEmpty()) {
                            pageFlow.value = -1
                            break
                        }
                        pageFlow.value++

                        dat = dat
                            .plus(newPosters.filter { filterer(it, filterData) }.map { projection(it, filterData) })
                            .toMutableList()
                            .distinctBy { it.id }
                    }
                }
                dataFlow.value = dat
                refreshStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                refreshStateFlow.value = SimpleState.Fail
            }
        }
    }

    override fun loadMore(data: A) {
        if (loadMoreStateFlow.value == SimpleState.Loading) return
        if (pageFlow.value == -1) {
            loadMoreStateFlow.value = SimpleState.Success
            return
        }
        loadMoreStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filterData = filterDataFlow.first()
                var finalData = dataFlow.value
                val initSize = finalData.size
                while (true) {
                    val newPosters = posterGetter(data, pageFlow.value.toLong())
                    if (newPosters.isEmpty()) {
                        pageFlow.value = -1
                        break
                    }
                    pageFlow.value++

                    finalData = finalData
                        .plus(newPosters.filter { filterer(it, filterData) }.map { projection(it, filterData) })
                        .toMutableList()
                        .distinctBy { it.id }
                    if (finalData.size - initSize >= loadMoreThreshold) break
                }
                dataFlow.value = finalData
                loadMoreStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                loadMoreStateFlow.value = SimpleState.Fail
            }
        }
    }
}

/**
 * 将*刷新*和*加载更多*的状态组合在一起
 */
abstract class RefreshAndLoadMoreStatesCombinedZero <T : UniqueData>(
    viewModelScope: CoroutineScope,
) : BasicRefreshAndLoadMoreStatesCombined<T>(viewModelScope) {

    /**
     * 将所有的状态暴露出来给组合函数
     */
    override fun export() = RefreshAndLoadMoreStatesCombinedExportDataZero(
        refreshStateFlow = refreshStateFlow.asStateFlow(),
        loadMoreStateFlow = loadMoreStateFlow.asStateFlow(),
        dataFlow = dataFlow.asStateFlow(),
        pageFlow = pageFlow.asStateFlow(),

        refresh = this::refresh,
        loadMore = this::loadMore,
    )

    abstract fun refresh()
    abstract fun loadMore()
}