package cn.bit101.android.ui.gallery.index

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.TabPagerItem
import cn.bit101.android.ui.component.rememberLoadableLazyColumnState
import cn.bit101.android.ui.gallery.common.RefreshState
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GalleryIndexScreen(
    mainController: MainController,
    onOpenImage: (Image) -> Unit,
    vm: GalleryIndexViewModel = hiltViewModel(),
) {
    val followRefreshState by vm.followStateCombined.refreshStateLiveData.observeAsState()
    val newestRefreshPostersState by vm.newestStataCombined.refreshStateLiveData.observeAsState()
    val hotRefreshPostersState by vm.hotStateCombined.refreshStateLiveData.observeAsState()
    val recommendRefreshPostersState by vm.recommendStateCombined.refreshStateLiveData.observeAsState()
    val searchRefreshPostersState by vm.searchStateCombined.refreshStateLiveData.observeAsState()

    val followLoadMoreState by vm.followStateCombined.loadMoreStateLiveData.observeAsState()
    val newestLoadMorePostersState by vm.newestStataCombined.loadMoreStateLiveData.observeAsState()
    val hotLoadMorePostersState by vm.hotStateCombined.loadMoreStateLiveData.observeAsState()
    val recommendLoadMorePostersState by vm.recommendStateCombined.loadMoreStateLiveData.observeAsState()
    val searchLoadMorePostersState by vm.searchStateCombined.loadMoreStateLiveData.observeAsState()

    val followPosters by vm.followStateCombined.dataLiveData.observeAsState()
    val newestPosters by vm.newestStataCombined.dataLiveData.observeAsState()
    val hotPosters by vm.hotStateCombined.dataLiveData.observeAsState()
    val recommendPosters by vm.recommendStateCombined.dataLiveData.observeAsState()
    val searchPosters by vm.searchStateCombined.dataLiveData.observeAsState()


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

    val _query by vm.queryLiveData.observeAsState()
    val _selectOrder by vm.selectOrderLiveData.observeAsState()

    val query = _query ?: ""
    val selectOrder = _selectOrder ?: PostersOrder.new

    val searchState = rememberLoadableLazyColumnState(
        refreshing = searchRefreshPostersState == RefreshState.Loading,
        onLoadMore = {
            vm.loadMoreSearch(query, selectOrder, PostersFilter.PUBLIC_ANONYMOUS)
        },
        onRefresh = {
            vm.refreshSearch(query, selectOrder, PostersFilter.PUBLIC_ANONYMOUS)
        }
    )

    val onOpenPoster: (Long) -> Unit = { id ->
        mainController.navController.navigate("poster/$id")
    }

    val onPost = {
        mainController.navController.navigate("post")
    }

    val pages = listOf(
        TabPagerItem("关注") {
            PostersTabPage(
                posters = followPosters ?: emptyList(),

                state = followState,
                refreshState = followRefreshState,
                loadState = followLoadMoreState,

                onRefresh = vm::refreshFollow,

                onOpenPoster = onOpenPoster,
                onOpenImage = onOpenImage,
                onPost = onPost,
            )
        },
        TabPagerItem("最新") {
            PostersTabPage(
                posters = newestPosters ?: emptyList(),
                state = newestState,
                refreshState = newestRefreshPostersState,
                loadState = newestLoadMorePostersState,

                onRefresh = vm::refreshNewest,
                onOpenPoster = onOpenPoster,
                onOpenImage = onOpenImage,
                onPost = onPost,
            )
        },
        TabPagerItem("推荐") {
            PostersTabPage(
                posters = recommendPosters ?: emptyList(),
                state = recommendState,
                refreshState = recommendRefreshPostersState,
                loadState = recommendLoadMorePostersState,

                onRefresh = vm::refreshRecommend,
                onOpenPoster = onOpenPoster,
                onOpenImage = onOpenImage,
                onPost = onPost,
            )
        },
        TabPagerItem("热门") {
            PostersTabPage(
                posters = hotPosters ?: emptyList(),
                state = hotState,
                refreshState = hotRefreshPostersState,
                loadState = hotLoadMorePostersState,

                onRefresh = vm::refreshHot,

                onOpenPoster = onOpenPoster,
                onOpenImage = onOpenImage,
                onPost = onPost,
            )
        },
        TabPagerItem("搜索") {
            SearchTabPage(
                posters = searchPosters ?: emptyList(),

                state = searchState,
                searchState = searchRefreshPostersState,
                loadState = searchLoadMorePostersState,
                query = query,
                selectOrder = selectOrder,

                onSearch = vm::refreshSearch,
                onQueryChange = vm::setQuery,
                onSelectOrderChange = vm::setSelectOrder,
                onOpenPoster = onOpenPoster,
                onOpenImage = onOpenImage,
                onPost = onPost,
            )
        },
    )
    var selectedTabIndex by remember { mutableIntStateOf(vm.initSelectedTabIndex) }

    Column {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            pages.forEachIndexed { index, page ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
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

        pages[selectedTabIndex].content(true)
    }
}