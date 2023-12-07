package cn.bit101.android.ui.component.loadable

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
internal fun BasicLoadableLazyColumn(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    loading: Boolean = false,
    refreshing: Boolean = false,
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
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text(
                        text = "加载中...",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
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

        if (pullRefreshState != null) lazyColumnModifier = lazyColumnModifier.pullRefresh(state = pullRefreshState)

        LazyColumn(
            modifier = lazyColumnModifier,
            contentPadding = contentPadding,
            state = lazyListState,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = {
                content()
                item { loadingContent() }
            },
        )
        pullRefreshIndicator()
    }
    if(loadMoreState != null) {
        // 获取 lazyList 布局信息
        val listLayoutInfo by remember { derivedStateOf { lazyListState.layoutInfo } }

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
                if (isScrollDown && remainCount <= loadMoreState.loadMoreRemainCountThreshold && pullRefreshState?.refreshing != true) {
                    LaunchedEffect(Unit) {
                        loadMoreState.onLoadMore()
                    }
                }
            }
            // 滑动结束后再更新值
            lastTimeLastVisibleIndex = currentLastVisibleIndex
        }
        lastTimeIsScrollInProgress = currentIsScrollInProgress
    }
}

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
    content: LazyListScope.() -> Unit,
) {
    BasicLoadableLazyColumn(
        modifier = modifier,
        lazyListState = state.lazyListState,
        loading = loading,
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