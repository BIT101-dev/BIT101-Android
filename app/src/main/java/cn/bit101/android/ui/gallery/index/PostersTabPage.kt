package cn.bit101.android.ui.gallery.index

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.LoadableLazyColumn
import cn.bit101.android.ui.component.LoadableLazyColumnState
import cn.bit101.android.ui.gallery.common.LoadMoreState
import cn.bit101.android.ui.gallery.common.RefreshState
import cn.bit101.android.ui.gallery.component.PosterCard
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import kotlinx.coroutines.launch

@Composable
fun PostersTabPage(
    mainController: MainController,
    nestedScrollConnection: NestedScrollConnection? = null,
    header: @Composable LazyItemScope.() -> Unit = {},

    /**
     * 帖子列表
     */
    posters: List<GetPostersDataModel.ResponseItem>,

    /**
     * 高亮的帖子id
     */
    highlightId: Long? = null,

    /**
     * 帖子列表的状态，这里既有下拉刷新又有上拉到底部加载更多
     */
    state: LoadableLazyColumnState,

    /**
     * 下拉刷新的状态
     */
    refreshState: RefreshState?,

    /**
     * 加载更多的状态
     */
    loadState: LoadMoreState?,

    /**
     * 打开图片组
     */
    onOpenImages: (Int, List<Image>) -> Unit,

    /**
     * 打开帖子
     */
    onOpenPoster: (Long) -> Unit,

    /**
     * 刷新帖子列表
     */
    onRefresh: () -> Unit,

    /**
     * 打开发帖或者编辑帖子的界面
     */
    onOpenPostOrEdit: () -> Unit,
) {
    val scope = rememberCoroutineScope() //供动画调用协程

    LaunchedEffect(refreshState) {
        if(refreshState == null) onRefresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            LoadableLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
                nestedScrollConnection = nestedScrollConnection,
                state = state,
                loading = loadState == LoadMoreState.Loading,
                refreshing = refreshState == RefreshState.Loading,
            ) {
                item("header") {
                    header()
                }
                itemsIndexed(posters, { idx, _ -> idx }) { _, it ->
                    if(it.id == highlightId) {
                        PosterCard(
                            data = it,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.secondary,
                                disabledContentColor = MaterialTheme.colorScheme.secondaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.secondary,
                            ),
                            onOpenPoster = { onOpenPoster(it.id) },
                            onOpenImages = onOpenImages,
                            onOpenUserDetail = { user ->
                                user?.let {
                                    mainController.navController.navigate("user/${it.id}")
                                }
                            }
                        )
                    } else {
                        PosterCard(
                            data = it,
                            onOpenPoster = { onOpenPoster(it.id) },
                            onOpenImages = onOpenImages,
                            onOpenUserDetail = { user ->
                                user?.let {
                                    mainController.navController.navigate("user/${it.id}")
                                }
                            }
                        )
                    }
                }
                item("spacer") {
                    Spacer(modifier = Modifier.padding(8.dp))
                }
            }
        }
        val fabSize = 42.dp
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp, 20.dp)
        ) {
            val show by remember { derivedStateOf { state.lazyListState.firstVisibleItemIndex > 1 } }
            AnimatedVisibility(
                visible = show,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SmallFloatingActionButton(
                    modifier = Modifier
                        .size(fabSize),
                    onClick = {
                        scope.launch {
                            state.lazyListState.animateScrollToItem(0, 0)
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

            SmallFloatingActionButton(
                modifier = Modifier
                    .size(fabSize),
                onClick = onOpenPostOrEdit,
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