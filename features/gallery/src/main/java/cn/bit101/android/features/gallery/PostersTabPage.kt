package cn.bit101.android.features.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.gallery.PosterCard
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumn
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import kotlinx.coroutines.launch

/**
 * 帖子列表的状态
 */
internal data class PostersState(
    /**
     * 帖子列表
     */
    val posters: List<GetPostersDataModel.ResponseItem>,

    /**
     * 帖子列表的状态，这里既有下拉刷新又有上拉到底部加载更多
     */
    val state: LoadableLazyColumnState,

    /**
     * 下拉刷新的状态
     */
    val refreshState: SimpleState?,

    /**
     * 加载更多的状态
     */
    val loadState: SimpleState?,

    /**
     * 刷新
     */
    val onRefresh: () -> Unit,
)

@Composable
internal fun PostersTabPage(
    mainController: MainController,

    postersState: PostersState,

    showPostButton: Boolean = true,

    /**
     * 打开帖子
     */
    onOpenPoster: (Long) -> Unit,

    /**
     * 打开发帖或者编辑帖子的界面
     */
    onOpenPostOrEdit: () -> Unit,
) {
    val scope = rememberCoroutineScope() //供动画调用协程

    LaunchedEffect(postersState.refreshState) {
        if(postersState.refreshState == null) postersState.onRefresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            LoadableLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally),
                error = postersState.refreshState is SimpleState.Fail,
                state = postersState.state,
                loading = postersState.loadState == SimpleState.Loading,
                refreshing = postersState.refreshState == SimpleState.Loading,
            ) {
                itemsIndexed(postersState.posters, { _, poster -> poster.id }) { _, it ->
                    PosterCard(
                        data = it,
                        onOpenPoster = { onOpenPoster(it.id) },
                        onOpenImages = mainController::showImages,
                        onOpenUserDetail = { user ->
                            user?.let {
                                mainController.navigate(NavDest.User(it.id.toLong()))
                            }
                        }
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }
        if(postersState.refreshState is SimpleState.Success) {
            val fabSize = 42.dp
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp, 20.dp, 10.dp, 20.dp)
            ) {
                val show by remember { derivedStateOf { postersState.state.lazyListState.firstVisibleItemIndex > 1 } }
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

                if(showPostButton) {
                    Spacer(modifier = Modifier.height(10.dp))

                    FloatingActionButton(
                        modifier = Modifier.size(fabSize),
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
    }
}