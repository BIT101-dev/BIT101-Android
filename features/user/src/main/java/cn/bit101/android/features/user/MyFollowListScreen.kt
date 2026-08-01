package cn.bit101.android.features.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.user.page.FollowerPage
import cn.bit101.android.features.user.page.FollowingPage
import cn.bit101.android.features.user.page.PosterPage

@Composable
fun MyFollowListScreen(
    mainController: MainController,
    type: String,
) {
    val vm: MyFollowListViewModel = hiltViewModel()

    val followings by vm.followingStateExport.dataFlow.collectAsState()
    val followingRefreshState by vm.followingStateExport.refreshStateFlow.collectAsState()
    val followingLoadMoreState by vm.followingStateExport.loadMoreStateFlow.collectAsState()

    val followers by vm.followerStateExport.dataFlow.collectAsState()
    val followerRefreshState by vm.followerStateExport.refreshStateFlow.collectAsState()
    val followerLoadMoreState by vm.followerStateExport.loadMoreStateFlow.collectAsState()

    val posters by vm.postersStateExport.dataFlow.collectAsState()
    val posterRefreshState by vm.postersStateExport.refreshStateFlow.collectAsState()
    val posterLoadMoreState by vm.postersStateExport.loadMoreStateFlow.collectAsState()

    when (type) {
        NavDest.FollowList.FOLLOWER -> {
            FollowerPage(
                mainController = mainController,
                followers = followers,
                state = rememberLoadableLazyColumnWithoutPullRequestState(
                    onLoadMore = { vm.followerStateExport.loadMore() }
                ),
                refreshState = followerRefreshState,
                loadMoreState = followerLoadMoreState,
                onRefresh = { vm.followerStateExport.refresh() },
                onDismiss = { mainController.popBackStack() },
                onClearState = {}
            )
        }

        NavDest.FollowList.FOLLOWING -> {
            FollowingPage(
                mainController = mainController,
                followings = followings,
                state = rememberLoadableLazyColumnWithoutPullRequestState(
                    onLoadMore = { vm.followingStateExport.loadMore() }
                ),
                refreshState = followingRefreshState,
                loadMoreState = followingLoadMoreState,
                onRefresh = { vm.followingStateExport.refresh() },
                onDismiss = { mainController.popBackStack() },
                onClearState = {}
            )
        }

        else -> {
        }
    }
}
