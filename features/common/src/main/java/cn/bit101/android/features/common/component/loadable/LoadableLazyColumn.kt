package cn.bit101.android.features.common.component.loadable

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.component.ErrorMessageForPage
import cn.bit101.android.features.common.component.loadable.pullrefresh.PullRefreshIndicator
import cn.bit101.android.features.common.component.loadable.pullrefresh.PullRefreshState
import cn.bit101.android.features.common.component.loadable.pullrefresh.pullRefresh


@Composable
internal fun BasicLoadableLazyColumn(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    loading: Boolean = false,
    refreshing: Boolean = false,
    error: Boolean = false,
    loadMoreState: LoadMoreState?,
    pullRefreshState: PullRefreshState?,
    nestedScrollConnection: NestedScrollConnection? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    loadingContent: @Composable () -> Unit = {
        if (loading) {
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "正在加载中",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    },
    pullRefreshIndicator: @Composable BoxScope.() -> Unit = {
        if(pullRefreshState != null) PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState,
        )
    },
    content: LazyListScope.() -> Unit,
) {
    Box(modifier = modifier) {

        var lazyColumnModifier: Modifier = Modifier

        if (nestedScrollConnection != null) lazyColumnModifier = lazyColumnModifier.nestedScroll(nestedScrollConnection)

        lazyColumnModifier = lazyColumnModifier.fillMaxSize()

        if (pullRefreshState != null) {
            lazyColumnModifier = lazyColumnModifier
                .graphicsLayer {
                    translationY = pullRefreshState.position
                }
                .pullRefresh(state = pullRefreshState)
        }

        var size: IntSize by remember { mutableStateOf(IntSize.Zero) }

        LazyColumn(
            modifier = lazyColumnModifier.onSizeChanged { size = it },
            contentPadding = contentPadding,
            state = lazyListState,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = {
                if(error) {
                    item {
                        var dpSize: DpSize
                        LocalDensity.current.run {
                            dpSize = DpSize(size.width.toDp(), size.height.toDp())
                        }
                        Box(
                            modifier = Modifier.size(
                                height = dpSize.height - contentPadding.calculateTopPadding() - contentPadding.calculateBottomPadding(),
                                width = dpSize.width
                            )
                        ) {
                            ErrorMessageForPage()
                        }
                    }
                } else {
                    content()
                    if(!refreshing) {
                        item { loadingContent() }
                    }
                }
            },
        )
        pullRefreshIndicator()
    }
    if(loadMoreState != null) {
        // 获取 lazyList 布局信息
        val listLayoutInfo by remember {
            derivedStateOf { lazyListState.layoutInfo }
        }

        // 上次最后一个可见的 index
        var lastTimeLastVisibleIndex by remember {
            mutableIntStateOf(listLayoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0)
        }

        // 上次最后一个可见的 offset
        var lastTimeLastVisibleOffset by remember {
            mutableIntStateOf(listLayoutInfo.visibleItemsInfo.lastOrNull()?.offset ?: 0)
        }

        // 当前是否正在滑动
        val currentIsScrollInProgress = lazyListState.isScrollInProgress

        // 当前最后一个可见的 index
        val currentLastVisibleIndex = listLayoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

        // 当前最后一个可见的 offset
        val currentLastVisibleOffset = listLayoutInfo.visibleItemsInfo.lastOrNull()?.offset ?: 0


        if (currentIsScrollInProgress) {
            val onBottom = listLayoutInfo.visibleItemsInfo.lastOrNull()?.let {
                listLayoutInfo.totalItemsCount - 1 - it.index <= loadMoreState.loadMoreRemainCountThreshold
            } ?: true

            val isScrollDown = currentLastVisibleIndex > lastTimeLastVisibleIndex ||
                    (currentLastVisibleIndex == lastTimeLastVisibleIndex && currentLastVisibleOffset > lastTimeLastVisibleOffset)

            if((onBottom || isScrollDown) && pullRefreshState?.refreshing != true) {
                LaunchedEffect(Unit) {
                    loadMoreState.onLoadMore()
                }
            }

            // 滑动结束后再更新值
            lastTimeLastVisibleIndex = currentLastVisibleIndex
            lastTimeLastVisibleOffset = currentLastVisibleOffset
        }
    }
}

@Composable
fun LoadableLazyColumnWithoutPullRequest(
    modifier: Modifier = Modifier,
    state: LoadableLazyColumnWithoutPullRequestState,
    loading: Boolean,
    error: Boolean = false,
    nestedScrollConnection: NestedScrollConnection? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    BasicLoadableLazyColumn(
        modifier = modifier,
        lazyListState = state.lazyListState,
        loading = loading,
        error = error,
        loadMoreState = state.loadMoreState,
        pullRefreshState = null,
        nestedScrollConnection = nestedScrollConnection,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

@Composable
fun LoadableLazyColumn(
    modifier: Modifier = Modifier,
    state: LoadableLazyColumnState,
    refreshing: Boolean,
    loading: Boolean,
    error: Boolean = false,
    nestedScrollConnection: NestedScrollConnection? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    BasicLoadableLazyColumn(
        modifier = modifier,
        lazyListState = state.lazyListState,
        loading = loading,
        error = error,
        loadMoreState = state.loadMoreState,
        refreshing = refreshing,
        pullRefreshState = state.pullRefreshState,
        nestedScrollConnection = nestedScrollConnection,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}