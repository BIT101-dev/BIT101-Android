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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.rememberLoadableLazyColumnState
import cn.bit101.android.ui.gallery.common.RefreshState
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun GalleryIndexScreen(
    mainController: MainController,
    onOpenImages: (Int, List<Image>) -> Unit,
    vm: GalleryIndexViewModel = hiltViewModel(),
) {
    val followRefreshState by vm.followStateCombined.refreshStateFlow.collectAsState()
    val newestRefreshPostersState by vm.newestStataCombined.refreshStateFlow.collectAsState()
    val hotRefreshPostersState by vm.hotStateCombined.refreshStateFlow.collectAsState()
    val recommendRefreshPostersState by vm.recommendStateCombined.refreshStateFlow.collectAsState()
    val searchRefreshPostersState by vm.searchStateCombined.refreshStateFlow.collectAsState()

    val followLoadMoreState by vm.followStateCombined.loadMoreStateFlow.collectAsState()
    val newestLoadMorePostersState by vm.newestStataCombined.loadMoreStateFlow.collectAsState()
    val hotLoadMorePostersState by vm.hotStateCombined.loadMoreStateFlow.collectAsState()
    val recommendLoadMorePostersState by vm.recommendStateCombined.loadMoreStateFlow.collectAsState()
    val searchLoadMorePostersState by vm.searchStateCombined.loadMoreStateFlow.collectAsState()

    val followPosters by vm.followStateCombined.dataFlow.collectAsState()
    val newestPosters by vm.newestStataCombined.dataFlow.collectAsState()
    val hotPosters by vm.hotStateCombined.dataFlow.collectAsState()
    val recommendPosters by vm.recommendStateCombined.dataFlow.collectAsState()
    val searchPosters by vm.searchStateCombined.dataFlow.collectAsState()


    val followState = rememberLoadableLazyColumnState(
        refreshing = followRefreshState == RefreshState.Loading,
        onLoadMore = vm::loadMoreFollow,
        onRefresh = vm::refreshFollow
    )
    val newestState = rememberLoadableLazyColumnState(
        refreshing = newestRefreshPostersState == RefreshState.Loading,
        onLoadMore = vm::loadMoreNewest,
        onRefresh = vm::refreshNewest
    )
    val hotState = rememberLoadableLazyColumnState(
        refreshing = hotRefreshPostersState == RefreshState.Loading,
        onLoadMore = vm::loadMoreHot,
        onRefresh = vm::refreshHot
    )
    val recommendState = rememberLoadableLazyColumnState(
        refreshing = recommendRefreshPostersState == RefreshState.Loading,
        onLoadMore = vm::loadMoreRecommend,
        onRefresh = vm::refreshRecommend
    )

    val query by vm.queryLiveData.observeAsState()
    val selectOrder by vm.selectOrderLiveData.observeAsState()
    val lastSearchQuery by vm.lastSearchQueryLiveData.observeAsState()

    val searchState = rememberLoadableLazyColumnState(
        refreshing = searchRefreshPostersState == RefreshState.Loading,
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
                posters = followPosters ?: emptyList(),

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
                posters = newestPosters ?: emptyList(),
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
                posters = recommendPosters ?: emptyList(),
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
                posters = hotPosters ?: emptyList(),
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
                posters = searchPosters ?: emptyList(),

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

        val nestedScrollConnection = rememberNestedScrollInteropConnection()

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