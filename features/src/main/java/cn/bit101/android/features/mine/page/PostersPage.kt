package cn.bit101.android.features.mine.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.MainController
import cn.bit101.android.features.common.SimpleState
import cn.bit101.android.features.component.common.CircularProgressIndicatorForPage
import cn.bit101.android.features.component.common.ErrorMessageForPage
import cn.bit101.android.features.component.gallery.PosterCard
import cn.bit101.android.features.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.features.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.api.model.http.bit101.GetPostersDataModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosterPage(
    mainController: MainController,

    posters: List<GetPostersDataModel.ResponseItem>,

    loadMoreState: SimpleState?,
    refreshState: SimpleState?,

    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit,
    onClearState: () -> Unit,
) {

    LaunchedEffect(Unit) {
        if (refreshState == null) {
            onRefresh()
        }
    }

    DisposableEffect(Unit) {
        onDispose { onClearState() }
    }

    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的帖子",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = refreshState !is SimpleState.Loading
                    ) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                scrollBehavior = topBarScrollBehavior,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (refreshState) {
                null, is SimpleState.Loading -> {
                    CircularProgressIndicatorForPage()
                }

                is SimpleState.Fail -> {
                    ErrorMessageForPage()
                }

                else -> {
                    LoadableLazyColumnWithoutPullRequest(
                        state = rememberLoadableLazyColumnWithoutPullRequestState(onLoadMore = onLoadMore),
                        loading = loadMoreState is SimpleState.Loading,
                    ) {
                        items(posters, { it.id }) {
                            PosterCard(
                                data = it,
                                onOpenPoster = { mainController.navigate("poster/${it.id}") },
                                onOpenImages = mainController::showImages
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
            }
        }
    }
}