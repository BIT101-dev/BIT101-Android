package cn.bit101.android.features.common.component.loadable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import cn.bit101.android.features.common.component.loadable.pullrefresh.PullRefreshDefaults
import cn.bit101.android.features.common.component.loadable.pullrefresh.PullRefreshState
import cn.bit101.android.features.common.component.loadable.pullrefresh.rememberPullRefreshState


@Composable
fun rememberLoadableLazyColumnState(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    refreshThreshold: Dp = PullRefreshDefaults.RefreshThreshold,
    refreshingOffset: Dp = PullRefreshDefaults.RefreshingOffset,
    loadMoreRemainCountThreshold: Int = 0,
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

data class LoadableLazyColumnState(
    val lazyListState: LazyListState,
    val pullRefreshState: PullRefreshState,
    val loadMoreState: LoadMoreState,
)

data class LoadableLazyColumnWithoutPullRequestState(
    val lazyListState: LazyListState,
    val loadMoreState: LoadMoreState,
)