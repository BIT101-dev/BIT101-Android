package cn.bit101.android.ui.gallery.index

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    header: @Composable LazyItemScope.() -> Unit = {},

    posters: List<GetPostersDataModel.ResponseItem>,
    highlightId: Long? = null,
    state: LoadableLazyColumnState,
    refreshState: RefreshState?,
    loadState: LoadMoreState?,

    onOpenPoster: (Long) -> Unit,
    onRefresh: () -> Unit,
    onOpenImage: (Image) -> Unit,
    onPost: () -> Unit,
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
                state = state,
                loading = loadState == LoadMoreState.Loading,
                refreshing = refreshState == RefreshState.Loading,
            ) {
                item("header") {
                    header()
                }
                itemsIndexed(posters, { idx, it -> idx }) { index, it ->

                    if(it.id == highlightId) {
                        PosterCard(
                            data = it,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.secondary,
                                disabledContentColor = MaterialTheme.colorScheme.secondaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.secondary,
                            ),
                            onOpenPoster = onOpenPoster,
                            onOpenImage = onOpenImage,
                        )
                    } else {
                        PosterCard(
                            data = it,
                            onOpenPoster = onOpenPoster,
                            onOpenImage = onOpenImage,
                        )
                    }
                }
            }
        }
        val fabSize = 42.dp
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp, 20.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            SmallFloatingActionButton(
                modifier = Modifier
                    .size(fabSize),
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