package cn.bit101.android.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.rememberImagePicker
import cn.bit101.android.ui.component.gallery.PosterCard
import cn.bit101.android.ui.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.loadable.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.topbar.BasicTwoRowsTopAppBar
import cn.bit101.android.ui.component.user.UserInfoContent
import cn.bit101.android.ui.component.user.UserInfoTopAppBar
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreenContent(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    posters: List<GetPostersDataModel.ResponseItem>,
    state: LoadableLazyColumnWithoutPullRequestState,
    loadState: SimpleState?,
    followState: SimpleState?,

    onFollow: () -> Unit,
    onOpenPoster: (Long) -> Unit,
    onOpenImages: (Int, List<Image>) -> Unit,
) {
    val topAppBarBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null,
    )

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarBehavior.nestedScrollConnection),
        topBar = {
            UserInfoTopAppBar(
                title = {
                    Column {
                        Spacer(modifier = Modifier.padding(4.dp))
                        UserInfoContent(
                            mainController = mainController,
                            data = data,
                            following = followState is SimpleState.Loading,
                            onFollow = onFollow,
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                    }
                },
                scrollBehavior = topAppBarBehavior,
                smallTitle = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = data.user.nickname,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { mainController.navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "more action"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LoadableLazyColumnWithoutPullRequest(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
            state = state,
            loading = loadState == SimpleState.Loading,
        ) {
            posters.forEachIndexed { index, poster ->
                item(index + 100) {
                    PosterCard(
                        data = poster,
                        onOpenPoster = { onOpenPoster(poster.id) },
                        onOpenImages = onOpenImages,
                    )
                    HorizontalDivider(
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

@Composable
fun UserScreen(
    mainController: MainController,
    vm: UserViewModel = hiltViewModel(),
    id: Long = 0,
) {

    val ctx = LocalContext.current

    val getUserInfoState by vm.getUserInfoStateFlow.collectAsState()

    val posters by vm.posterStateExport.dataFlow.collectAsState()
    val postersRefreshState by vm.posterStateExport.refreshStateFlow.collectAsState()
    val postersLoadMoreState by vm.posterStateExport.loadMoreStateFlow.collectAsState()

    val followState by vm.followStateMutableLiveData.observeAsState()

    val uploadUserInfoState by vm.uploadUserInfoStateLiveData.observeAsState()

    DisposableEffect(uploadUserInfoState) {
        if (uploadUserInfoState is SimpleState.Success) {
            vm.uploadUserInfoStateLiveData.value = null
            mainController.snackbar("保存成功OvO")
        } else if (uploadUserInfoState is SimpleState.Fail) {
            vm.uploadUserInfoStateLiveData.value = null
            mainController.snackbar("保存失败Orz")
        }
        onDispose { }
    }


    LaunchedEffect(getUserInfoState) {
        if (getUserInfoState == null) {
            vm.getUserInfo(id)
        }
    }

    LaunchedEffect(postersRefreshState) {
        if (postersRefreshState == null) {
            vm.posterStateExport.refresh(id)
        }
    }

    if(getUserInfoState == null || postersRefreshState == null) {
        return
    } else if(getUserInfoState is SimpleDataState.Loading || postersRefreshState is SimpleState.Loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .width(64.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else if(getUserInfoState is SimpleDataState.Success && postersRefreshState is SimpleState.Success) {
        UserScreenContent(
            mainController = mainController,
            data = (getUserInfoState as SimpleDataState.Success).data,
            posters = posters,

            state = rememberLoadableLazyColumnWithoutPullRequestState(
                onLoadMore = { vm.posterStateExport.loadMore(id) }
            ),
            loadState = postersLoadMoreState,
            followState = followState,

            onOpenImages = mainController::showImages,
            onOpenPoster = { mainController.navController.navigate("poster/$it") },
            onFollow = { vm.follow(id) },
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "加载失败",
                textAlign = TextAlign.Center
            )
        }
    }
}