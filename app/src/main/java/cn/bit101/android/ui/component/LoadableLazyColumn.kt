package cn.bit101.android.ui.component

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import cn.bit101.android.ui.component.pullrefresh.pullRefresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.component.pullrefresh.PullRefreshDefaults
import cn.bit101.android.ui.component.pullrefresh.PullRefreshIndicator
import cn.bit101.android.ui.component.pullrefresh.PullRefreshState
import cn.bit101.android.ui.component.pullrefresh.rememberPullRefreshState

@Composable
fun LoadableLazyColumnWithoutPullRequest(
    modifier: Modifier = Modifier,
    state: LoadableLazyColumnWithoutPullRequestState,
    loading: Boolean,
    nestedScrollConnection: NestedScrollConnection? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    loadingContent: (@Composable () -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val lazyListState = state.lazyListState
    // 获取 lazyList 布局信息
    val listLayoutInfo by remember { derivedStateOf { lazyListState.layoutInfo } }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = (if(nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier)
                .fillMaxSize(),
            contentPadding = contentPadding,
            state = state.lazyListState,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = {
                content()
                item {
                    if (loadingContent != null) { loadingContent() }
                    else {
                        if (loading) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.align(Alignment.Center)) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(2.dp))
                                    Text(
                                        text = "加载中...",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MaterialTheme.typography.titleSmall.color.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )
    }
    // 上次是否正在滑动
    var lastTimeIsScrollInProgress by remember {
        mutableStateOf(lazyListState.isScrollInProgress)
    }
    // 上次滑动结束后最后一个可见的index
    var lastTimeLastVisibleIndex by remember {
        mutableIntStateOf(listLayoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0)
    }
    // 当前是否正在滑动
    val currentIsScrollInProgress = lazyListState.isScrollInProgress
    // 当前最后一个可见的 index
    val currentLastVisibleIndex = listLayoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
    if (!currentIsScrollInProgress && lastTimeIsScrollInProgress) {
        if (currentLastVisibleIndex != lastTimeLastVisibleIndex) {
            val isScrollDown = currentLastVisibleIndex > lastTimeLastVisibleIndex
            val remainCount = listLayoutInfo.totalItemsCount - currentLastVisibleIndex - 1
            if (isScrollDown && remainCount <= state.loadMoreState.loadMoreRemainCountThreshold) {
                LaunchedEffect(Unit) {
                    state.loadMoreState.onLoadMore()
                }
            }
        }
        // 滑动结束后再更新值
        lastTimeLastVisibleIndex = currentLastVisibleIndex
    }
    lastTimeIsScrollInProgress = currentIsScrollInProgress
}

@Composable
fun LoadableLazyColumn(
    modifier: Modifier = Modifier,
    state: LoadableLazyColumnState,
    refreshing: Boolean,
    loading: Boolean,
    nestedScrollConnection: NestedScrollConnection? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    loadingContent: (@Composable () -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val lazyListState = state.lazyListState
    // 获取 lazyList 布局信息
    val listLayoutInfo by remember { derivedStateOf { lazyListState.layoutInfo } }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = (if (nestedScrollConnection != null) Modifier.nestedScroll(
                nestedScrollConnection
            ) else Modifier)
                .fillMaxSize()
                .pullRefresh(state = state.pullRefreshState),
            contentPadding = contentPadding,
            state = state.lazyListState,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = {
                content()
                item {
                    if (loadingContent != null) {
                        loadingContent()
                    } else {
                        if (loading) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.align(Alignment.Center)) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(2.dp))
                                    Text(
                                        text = "加载中...",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MaterialTheme.typography.titleSmall.color.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = state.pullRefreshState,
        )
    }
    // 上次是否正在滑动
    var lastTimeIsScrollInProgress by remember {
        mutableStateOf(lazyListState.isScrollInProgress)
    }
    // 上次滑动结束后最后一个可见的index
    var lastTimeLastVisibleIndex by remember {
        mutableIntStateOf(listLayoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0)
    }
    // 当前是否正在滑动
    val currentIsScrollInProgress = lazyListState.isScrollInProgress
    // 当前最后一个可见的 index
    val currentLastVisibleIndex = listLayoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
    if (!currentIsScrollInProgress && lastTimeIsScrollInProgress) {
        if (currentLastVisibleIndex != lastTimeLastVisibleIndex) {
            val isScrollDown = currentLastVisibleIndex > lastTimeLastVisibleIndex
            val remainCount = listLayoutInfo.totalItemsCount - currentLastVisibleIndex - 1
            if (isScrollDown && remainCount <= state.loadMoreState.loadMoreRemainCountThreshold) {
                LaunchedEffect(Unit) {
                    state.loadMoreState.onLoadMore()
                }
            }
        }
        // 滑动结束后再更新值
        lastTimeLastVisibleIndex = currentLastVisibleIndex
    }
    lastTimeIsScrollInProgress = currentIsScrollInProgress
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberLoadableLazyColumnState(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    refreshThreshold: Dp = PullRefreshDefaults.RefreshThreshold,
    refreshingOffset: Dp = PullRefreshDefaults.RefreshingOffset,
    loadMoreRemainCountThreshold: Int = 5,
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
): LoadableLazyColumnState {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = onRefresh,
        refreshThreshold = refreshThreshold,
        refreshingOffset = refreshingOffset,
    )

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset,
        initialFirstVisibleItemIndex = initialFirstVisibleItemIndex,
    )

    val loadMoreState = rememberLoadMoreState(loadMoreRemainCountThreshold, onLoadMore)

    return remember {
        LoadableLazyColumnState(
            lazyListState = lazyListState,
            pullRefreshState = pullRefreshState,
            loadMoreState = loadMoreState,
        )
    }
}

@Composable
fun rememberLoadableLazyColumnWithoutPullRequestState(
    onLoadMore: () -> Unit,
    loadMoreRemainCountThreshold: Int = 5,
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0
): LoadableLazyColumnWithoutPullRequestState {
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset,
        initialFirstVisibleItemIndex = initialFirstVisibleItemIndex,
    )

    val loadMoreState = rememberLoadMoreState(loadMoreRemainCountThreshold, onLoadMore)

    return remember {
        LoadableLazyColumnWithoutPullRequestState(
            lazyListState = lazyListState,
            loadMoreState = loadMoreState,
        )
    }
}

@Composable
fun rememberLoadMoreState(
    loadMoreRemainCountThreshold: Int,
    onLoadMore: () -> Unit,
): LoadMoreState {
    return remember {
        LoadMoreState(loadMoreRemainCountThreshold, onLoadMore)
    }
}

data class LoadMoreState(
    val loadMoreRemainCountThreshold: Int,
    val onLoadMore: () -> Unit,
)

data class LoadableLazyColumnState constructor(
    val lazyListState: LazyListState,
    val pullRefreshState: PullRefreshState,
    val loadMoreState: LoadMoreState,
)

data class LoadableLazyColumnWithoutPullRequestState(
    val lazyListState: LazyListState,
    val loadMoreState: LoadMoreState,
)