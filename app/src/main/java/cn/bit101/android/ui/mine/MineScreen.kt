package cn.bit101.android.ui.mine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.common.AnimatedPage
import cn.bit101.android.ui.component.loadable.rememberLoadableLazyColumnState
import cn.bit101.android.ui.mine.page.FollowerPage
import cn.bit101.android.ui.mine.page.FollowingPage

private interface PageIndex {
    companion object {
        const val MINE = 0
        const val FOLLOWING = 1
        const val FOLLOWER = 2
    }
}

private fun Int.isMainPage() =
    this == PageIndex.MINE || this > PageIndex.FOLLOWER

@Composable
fun MineScreen(
    mainController: MainController,
    vm: MineViewModel = hiltViewModel(),
) {
    val userInfoState by vm.userInfoStateLiveData.observeAsState()

    var pageState by rememberSaveable { mutableStateOf(PageIndex.MINE) }

    val followings by vm.followingStateExports.dataFlow.collectAsState()
    val followingRefreshState by vm.followingStateExports.refreshStateFlow.collectAsState()
    val followingLoadMoreState by vm.followingStateExports.loadMoreStateFlow.collectAsState()

    val followers by vm.followerStateExports.dataFlow.collectAsState()
    val followerRefreshState by vm.followerStateExports.refreshStateFlow.collectAsState()
    val followerLoadMoreState by vm.followerStateExports.loadMoreStateFlow.collectAsState()


    LaunchedEffect(Unit) {
        vm.updateUserInfo()
    }

    if(userInfoState == null) {
        return
    } else if(userInfoState is SimpleDataState.Success) {
        val data = (userInfoState as SimpleDataState.Success).data
        AnimatedPage(
            page = pageState,
            isMainPage = pageState == PageIndex.MINE,
            label = "poster screen content",
        ) { page ->
            when(page) {
                PageIndex.FOLLOWER -> {
                    FollowerPage(
                        mainController = mainController,
                        followers = followers,
                        state = rememberLoadableLazyColumnState(
                            onRefresh = { vm.followerStateExports.refresh() },
                            refreshing = followerRefreshState is SimpleState.Loading,
                            onLoadMore = { vm.followerStateExports.loadMore() }
                        ),
                        refreshState = followerRefreshState,
                        loadMoreState = followerLoadMoreState,
                        onRefresh = { vm.followerStateExports.refresh() },
                        onDismiss = { pageState = PageIndex.MINE },
                        onClearState = {}
                    )
                }
                PageIndex.FOLLOWING -> {
                    FollowingPage(
                        mainController = mainController,
                        followings = followings,
                        state = rememberLoadableLazyColumnState(
                            onRefresh = { vm.followingStateExports.refresh() },
                            refreshing = followingRefreshState is SimpleState.Loading,
                            onLoadMore = { vm.followingStateExports.loadMore() }
                        ),
                        refreshState = followingRefreshState,
                        loadMoreState = followingLoadMoreState,
                        onRefresh = { vm.followingStateExports.refresh() },
                        onDismiss = { pageState = PageIndex.MINE },
                        onClearState = {}
                    )
                }
                else -> {
                    MineScreenContent(
                        mainController = mainController,
                        data = data,
                        onOpenFollowerDialog = { if(pageState.isMainPage()) pageState = PageIndex.FOLLOWER },
                        onOpenFollowingDialog = { if(pageState.isMainPage()) pageState = PageIndex.FOLLOWING }
                    )
                }
            }
        }

    } else if (userInfoState is SimpleDataState.Loading) {
    }
}