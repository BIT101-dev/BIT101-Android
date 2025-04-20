package cn.bit101.android.features.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.AnimatedPage
import cn.bit101.android.features.common.component.loadable.rememberLoadableLazyColumnState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.nav.NavDest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch


internal data class TabPagerItem(
    val title: String,
    val content: @Composable () -> Unit
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    mainController: MainController,
) {
    val vm: GalleryIndexViewModel = hiltViewModel()

    val followRefreshState by vm.followStateExport.refreshStateFlow.collectAsState()
    val newestRefreshPostersState by vm.newestStataExport.refreshStateFlow.collectAsState()
    val hotRefreshPostersState by vm.hotStateExport.refreshStateFlow.collectAsState()
    val recommendRefreshPostersState by vm.recommendStateExport.refreshStateFlow.collectAsState()
    val searchRefreshPostersState by vm.searchStateExports.refreshStateFlow.collectAsState()

    val followLoadMoreState by vm.followStateExport.loadMoreStateFlow.collectAsState()
    val newestLoadMorePostersState by vm.newestStataExport.loadMoreStateFlow.collectAsState()
    val hotLoadMorePostersState by vm.hotStateExport.loadMoreStateFlow.collectAsState()
    val recommendLoadMorePostersState by vm.recommendStateExport.loadMoreStateFlow.collectAsState()
    val searchLoadMorePostersState by vm.searchStateExports.loadMoreStateFlow.collectAsState()

    val followPosters by vm.followStateExport.dataFlow.collectAsState()
    val newestPosters by vm.newestStataExport.dataFlow.collectAsState()
    val hotPosters by vm.hotStateExport.dataFlow.collectAsState()
    val recommendPosters by vm.recommendStateExport.dataFlow.collectAsState()
    val searchPosters by vm.searchStateExports.dataFlow.collectAsState()

    var searchData by rememberSaveable { mutableStateOf(SearchData.default) }

    val followState = rememberLoadableLazyColumnState(
        refreshing = followRefreshState == SimpleState.Loading,
        onLoadMore = vm.followStateExport.loadMore,
        onRefresh = vm.followStateExport.refresh,
    )
    val newestState = rememberLoadableLazyColumnState(
        refreshing = newestRefreshPostersState == SimpleState.Loading,
        onLoadMore = vm.newestStataExport.loadMore,
        onRefresh = vm.newestStataExport.refresh
    )
    val hotState = rememberLoadableLazyColumnState(
        refreshing = hotRefreshPostersState == SimpleState.Loading,
        onLoadMore = vm.hotStateExport.loadMore,
        onRefresh = vm.hotStateExport.refresh
    )
    val recommendState = rememberLoadableLazyColumnState(
        refreshing = recommendRefreshPostersState == SimpleState.Loading,
        onLoadMore = vm.recommendStateExport.loadMore,
        onRefresh = vm.recommendStateExport.refresh
    )

    val searchState = rememberLoadableLazyColumnState(
        refreshing = searchRefreshPostersState == SimpleState.Loading,
        onLoadMore = { vm.searchStateExports.loadMore(searchData) },
        onRefresh = { vm.searchStateExports.refresh(searchData) }
    )

    var showSearchPageState by rememberSaveable { mutableStateOf(false) }

    val onOpenPoster: (Long) -> Unit = { id ->
        mainController.navigate(NavDest.Poster(id))
    }

    val onPost: () -> Unit = {
        mainController.navigate(NavDest.Post)
    }

    val posterStates = listOf(
        PostersState(
            posters = followPosters,
            state = followState,
            refreshState = followRefreshState,
            loadState = followLoadMoreState,
            onRefresh = vm.followStateExport.refresh,
        ),
        PostersState(
            posters = recommendPosters,
            state = recommendState,
            refreshState = recommendRefreshPostersState,
            loadState = recommendLoadMorePostersState,
            onRefresh = vm.recommendStateExport.refresh,
        ),
        PostersState(
            posters = newestPosters,
            state = newestState,
            refreshState = newestRefreshPostersState,
            loadState = newestLoadMorePostersState,
            onRefresh = vm.newestStataExport.refresh,
        ),
        PostersState(
            posters = hotPosters,
            state = hotState,
            refreshState = hotRefreshPostersState,
            loadState = hotLoadMorePostersState,
            onRefresh = vm.hotStateExport.refresh,
        )
    )

    val pages = listOf(
        TabPagerItem("关注") {
            PostersTabPage(
                mainController = mainController,
                postersState = posterStates[0],
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
        TabPagerItem("推荐") {
            PostersTabPage(
                mainController = mainController,
                postersState = posterStates[1],
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
        TabPagerItem("最新") {
            PostersTabPage(
                mainController = mainController,
                postersState = posterStates[2],
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        },
        TabPagerItem("最热") {
            PostersTabPage(
                mainController = mainController,
                postersState = posterStates[3],
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        }
    )

    val horizontalPagerState = rememberPagerState(
        pageCount = { pages.size },
        initialPage = 1,
    )
    val scope = rememberCoroutineScope()

    AnimatedPage(
        page = showSearchPageState,
        isMainPage = !showSearchPageState,
        label = "search page",
        onDismiss = { showSearchPageState = false }
    ) { showSearchPage ->
        if(showSearchPage) {
            SearchPage(
                mainController = mainController,
                searchData = searchData,
                onSearch = {
                    scope.launch {
                        searchState.lazyListState.scrollToItem(0)
                        vm.searchStateExports.refresh(it)
                    }
                },
                onSearchDataChanged = { searchData = it },
                onOpenPoster = onOpenPoster,
                onPost = onPost,
                state = PostersState(
                    posters = searchPosters,
                    state = searchState,
                    refreshState = searchRefreshPostersState,
                    loadState = searchLoadMorePostersState,
                    onRefresh = { vm.searchStateExports.refresh(searchData) },
                ),
                onDismiss = { showSearchPageState = false }
            )
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            TabRow(
                                modifier = Modifier.width(280.dp),
                                selectedTabIndex = horizontalPagerState.currentPage,
                                divider = {},
                                containerColor = Color.Transparent,
                            ) {
                                pages.forEachIndexed { index, page ->
                                    Tab(
                                        selected = horizontalPagerState.currentPage == index,
                                        onClick = {
                                            scope.launch {
                                                horizontalPagerState.scrollToPage(index, 0f)
                                            }
                                        },
                                        text = {
                                            Text(
                                                text = page.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = if (horizontalPagerState.currentPage == index) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                    ),
                                                    fontWeight = if (horizontalPagerState.currentPage == index) MaterialTheme.typography.titleMedium.fontWeight
                                                    else FontWeight.Bold,
                                                )
                                            )
                                        },
                                        //禁用水波纹特效
                                        interactionSource = remember {
                                            object : MutableInteractionSource {
                                                override val interactions: Flow<Interaction> =
                                                    emptyFlow()

                                                override suspend fun emit(interaction: Interaction) {}
                                                override fun tryEmit(interaction: Interaction) =
                                                    true
                                            }
                                        },
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSearchPageState = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "搜索"
                                )
                            }
                        },
                    )
                }
            ) { paddingValues ->
                val top = paddingValues.calculateTopPadding()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = top)
                ) {
                    HorizontalPager(
                        state = horizontalPagerState,
                    ) { index ->
                        pages[index].content()
                    }

                    val postersState = posterStates[horizontalPagerState.currentPage]

                    if(postersState.refreshState is SimpleState.Success) {
                        val fabSize = 42.dp
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp, 20.dp, 10.dp, 20.dp)
                        ) {
                            val show by remember { derivedStateOf { posterStates[horizontalPagerState.currentPage].state.lazyListState.firstVisibleItemIndex > 1 } }
                            AnimatedVisibility(
                                visible = show,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                FloatingActionButton(
                                    modifier = Modifier
                                        .size(fabSize),
                                    onClick = {
                                        scope.launch {
                                            postersState.state.lazyListState.animateScrollToItem(0, 0)
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowUpward,
                                        contentDescription = "回到顶部"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            FloatingActionButton(
                                modifier = Modifier.size(fabSize),
                                onClick = onPost,
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                                contentColor = MaterialTheme.colorScheme.primary,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "张贴Poster"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}