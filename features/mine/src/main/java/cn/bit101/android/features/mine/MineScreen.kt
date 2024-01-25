package cn.bit101.android.features.mine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.AnimatedPage
import cn.bit101.android.features.common.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.mine.page.FollowerPage
import cn.bit101.android.features.mine.page.FollowingPage
import cn.bit101.android.features.mine.page.PosterPage

private interface PageIndex {
    companion object {
        const val MINE = 0
        const val FOLLOWING = 1
        const val FOLLOWER = 2
        const val POSTERS = 3
    }
}

private fun Int.isMainPage() =
    this == PageIndex.MINE || this > PageIndex.FOLLOWER

@Composable
fun MineScreen(mainController: MainController) {
    val vm: MineViewModel = hiltViewModel()

    val userInfoState by vm.userInfoStateLiveData.observeAsState()

    var pageState by rememberSaveable { mutableIntStateOf(PageIndex.MINE) }

    val followings by vm.followingStateExports.dataFlow.collectAsState()
    val followingRefreshState by vm.followingStateExports.refreshStateFlow.collectAsState()
    val followingLoadMoreState by vm.followingStateExports.loadMoreStateFlow.collectAsState()

    val followers by vm.followerStateExports.dataFlow.collectAsState()
    val followerRefreshState by vm.followerStateExports.refreshStateFlow.collectAsState()
    val followerLoadMoreState by vm.followerStateExports.loadMoreStateFlow.collectAsState()

    val posters by vm.postersStateExports.dataFlow.collectAsState()
    val posterRefreshState by vm.postersStateExports.refreshStateFlow.collectAsState()
    val posterLoadMoreState by vm.postersStateExports.loadMoreStateFlow.collectAsState()

    val messageCountState by vm.messageCountStateLiveData.observeAsState()


    LaunchedEffect(Unit) {
        vm.updateUserInfo()
    }

    LaunchedEffect(Unit) {
        vm.updateMessageCount()
    }

    if(userInfoState == null) return

    AnimatedPage(
        page = pageState,
        isMainPage = pageState == PageIndex.MINE,
        label = "poster screen content",
        onDismiss = { pageState = PageIndex.MINE }
    ) { page ->
        when(page) {
            PageIndex.FOLLOWER -> {
                FollowerPage(
                    mainController = mainController,
                    followers = followers,
                    state = rememberLoadableLazyColumnWithoutPullRequestState(
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
                    state = rememberLoadableLazyColumnWithoutPullRequestState(
                        onLoadMore = { vm.followingStateExports.loadMore() }
                    ),
                    refreshState = followingRefreshState,
                    loadMoreState = followingLoadMoreState,
                    onRefresh = { vm.followingStateExports.refresh() },
                    onDismiss = { pageState = PageIndex.MINE },
                    onClearState = {}
                )
            }

            PageIndex.POSTERS -> {
                PosterPage(
                    mainController = mainController,
                    posters = posters,
                    loadMoreState = posterLoadMoreState,
                    refreshState = posterRefreshState,
                    onRefresh = { vm.postersStateExports.refresh() },
                    onLoadMore = { vm.postersStateExports.loadMore() },
                    onDismiss = { pageState = PageIndex.MINE },
                    onClearState = {}
                )
            }

            else -> {
                val messageCount = (messageCountState as? SimpleDataState.Success)?.data
                MineScreenContent(
                    mainController = mainController,
                    messageCount = messageCount ?: 0,
                    userInfoState = userInfoState!!,
                    onRefresh = vm::updateUserInfo,
                    onOpenFollowerDialog = { if(pageState.isMainPage()) pageState = PageIndex.FOLLOWER },
                    onOpenFollowingDialog = { if(pageState.isMainPage()) pageState = PageIndex.FOLLOWING },
                    onOpenPostersDialog = { if(pageState.isMainPage()) pageState = PageIndex.POSTERS },
                    onOpenMessagePage = { mainController.navigate("message") }
                )
            }
        }
    }
}