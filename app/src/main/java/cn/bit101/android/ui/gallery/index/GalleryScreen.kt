package cn.bit101.android.ui.gallery.index

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.rememberLoadableLazyColumnState
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch


data class TabPagerItemWithNestedScroll(
    val title: String,
    val content: @Composable (NestedScrollConnection?) -> Unit
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    mainController: MainController,
    onOpenImages: (Int, List<Image>) -> Unit,
    navBarHeight: Dp = 0.dp,
    vm: GalleryIndexViewModel = hiltViewModel(),
) {
    val followRefreshState by vm.followStateFlows.refreshStateFlow.collectAsState()
    val newestRefreshPostersState by vm.newestStataFlows.refreshStateFlow.collectAsState()
    val hotRefreshPostersState by vm.hotStateFlows.refreshStateFlow.collectAsState()
    val recommendRefreshPostersState by vm.recommendStateFlows.refreshStateFlow.collectAsState()
    val searchRefreshPostersState by vm.searchStateFlows.refreshStateFlow.collectAsState()

    val followLoadMoreState by vm.followStateFlows.loadMoreStateFlow.collectAsState()
    val newestLoadMorePostersState by vm.newestStataFlows.loadMoreStateFlow.collectAsState()
    val hotLoadMorePostersState by vm.hotStateFlows.loadMoreStateFlow.collectAsState()
    val recommendLoadMorePostersState by vm.recommendStateFlows.loadMoreStateFlow.collectAsState()
    val searchLoadMorePostersState by vm.searchStateFlows.loadMoreStateFlow.collectAsState()

    val followPosters by vm.followStateFlows.dataFlow.collectAsState()
    val newestPosters by vm.newestStataFlows.dataFlow.collectAsState()
    val hotPosters by vm.hotStateFlows.dataFlow.collectAsState()
    val recommendPosters by vm.recommendStateFlows.dataFlow.collectAsState()
    val searchPosters by vm.searchStateFlows.dataFlow.collectAsState()


    val followState = rememberLoadableLazyColumnState(
        refreshing = followRefreshState == SimpleState.Loading,
        onLoadMore = vm::loadMoreFollow,
        onRefresh = vm::refreshFollow
    )
    val newestState = rememberLoadableLazyColumnState(
        refreshing = newestRefreshPostersState == SimpleState.Loading,
        onLoadMore = vm::loadMoreNewest,
        onRefresh = vm::refreshNewest
    )
    val hotState = rememberLoadableLazyColumnState(
        refreshing = hotRefreshPostersState == SimpleState.Loading,
        onLoadMore = vm::loadMoreHot,
        onRefresh = vm::refreshHot
    )
    val recommendState = rememberLoadableLazyColumnState(
        refreshing = recommendRefreshPostersState == SimpleState.Loading,
        onLoadMore = vm::loadMoreRecommend,
        onRefresh = vm::refreshRecommend
    )

    val query by vm.queryLiveData.observeAsState()
    val selectOrder by vm.selectOrderLiveData.observeAsState()
    val lastSearchQuery by vm.lastSearchQueryLiveData.observeAsState()

    val searchState = rememberLoadableLazyColumnState(
        refreshing = searchRefreshPostersState == SimpleState.Loading,
        onLoadMore = {
            vm.loadMoreSearch(
                query ?: "",
                selectOrder ?: PostersOrder.NEW,
                PostersFilter.PUBLIC_ANONYMOUS
            )
        },
        onRefresh = {
            vm.refreshSearch(
                query ?: "",
                selectOrder ?: PostersOrder.NEW,
                PostersFilter.PUBLIC_ANONYMOUS
            )
        }
    )

    val onOpenPoster: (Long) -> Unit = { id ->
        mainController.navController.navigate("poster/$id")
    }

    val onPost = {
        mainController.navController.navigate("post")
    }

    val pages = listOf(
        TabPagerItemWithNestedScroll("关注") {
            PostersTabPage(
                mainController = mainController,
                nestedScrollConnection = it,
                navBarHeight = navBarHeight,
                posters = followPosters,

                state = followState,
                refreshState = followRefreshState,
                loadState = followLoadMoreState,

                onRefresh = vm::refreshFollow,

                onOpenImages = onOpenImages,
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
        TabPagerItemWithNestedScroll("最新") {
            PostersTabPage(
                mainController = mainController,
                nestedScrollConnection = it,
                navBarHeight = navBarHeight,
                posters = newestPosters,
                state = newestState,
                refreshState = newestRefreshPostersState,
                loadState = newestLoadMorePostersState,

                onOpenImages = onOpenImages,
                onRefresh = vm::refreshNewest,
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
        TabPagerItemWithNestedScroll("推荐") {
            PostersTabPage(
                mainController = mainController,
                nestedScrollConnection = it,
                navBarHeight = navBarHeight,
                posters = recommendPosters,
                state = recommendState,
                refreshState = recommendRefreshPostersState,
                loadState = recommendLoadMorePostersState,

                onOpenImages = onOpenImages,
                onRefresh = vm::refreshRecommend,
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
        TabPagerItemWithNestedScroll("热门") {
            PostersTabPage(
                mainController = mainController,
                nestedScrollConnection = it,
                navBarHeight = navBarHeight,
                posters = hotPosters,
                state = hotState,
                refreshState = hotRefreshPostersState,
                loadState = hotLoadMorePostersState,

                onRefresh = vm::refreshHot,

                onOpenImages = onOpenImages,
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
        TabPagerItemWithNestedScroll("搜索") {
            SearchTabPage(
                mainController = mainController,
                nestedScrollConnection = it,
                navBarHeight = navBarHeight,
                posters = searchPosters,

                state = searchState,
                searchState = searchRefreshPostersState,
                loadState = searchLoadMorePostersState,

                query = query ?: "",
                selectOrder = selectOrder ?: PostersOrder.NEW,
                lastSearchQuery = lastSearchQuery ?: "",

                onSearch = vm::refreshSearch,
                onQueryChange = vm::setQuery,
                onSelectOrderChange = vm::setSelectOrder,
                onOpenImages = onOpenImages,
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
    )

    val horizontalPagerState = rememberPagerState(
        pageCount = { pages.size },
        initialPage = vm.initSelectedTabIndex,
    )
    val scope = rememberCoroutineScope()

    Column {
        PrimaryTabRow(
            selectedTabIndex = horizontalPagerState.currentPage,
        ) {
            pages.forEachIndexed { index, page ->
                Tab(
                    selected = horizontalPagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            horizontalPagerState.scrollToPage(index, 0f)
                        }
                        vm.initSelectedTabIndex = index
                    },
                    text = { Text(text = page.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                    //禁用水波纹特效
                    interactionSource = remember {
                        object : MutableInteractionSource {
                            override val interactions: Flow<Interaction> = emptyFlow()
                            override suspend fun emit(interaction: Interaction) {}
                            override fun tryEmit(interaction: Interaction) = true
                        }
                    },
                )
            }
        }

        //禁用overscroll阴影效果
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            HorizontalPager(
                state = horizontalPagerState,
                userScrollEnabled = false,
            ) { index ->
                pages[index].content(null)
            }
        }
    }
}