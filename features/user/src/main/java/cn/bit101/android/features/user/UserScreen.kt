package cn.bit101.android.features.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.CircularProgressIndicatorForPage
import cn.bit101.android.features.common.component.ErrorMessageForPage
import cn.bit101.android.features.common.component.gallery.PosterCard
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.features.common.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.features.common.component.user.UserInfoContent
import cn.bit101.android.features.common.component.user.UserInfoTopAppBar
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserScreenContent(
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
    val cm = LocalClipboardManager.current

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
                            data = data,
                            following = followState is SimpleState.Loading,
                            onFollow = onFollow,
                            onCopyText = { mainController.copyText(cm, it) },
                            onShowImage = { mainController.showImage(it) },
                            onOpenPoster = { mainController.navigate("poster/$it") },
                            onOpenUser = { mainController.navigate("user/$it") },
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
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                actions = {
//                    IconButton(onClick = {}) {
//                        Icon(
//                            imageVector = Icons.Rounded.MoreVert,
//                            contentDescription = "more action"
//                        )
//                    }
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

@Composable
fun UserScreen(
    mainController: MainController,
    id: Long = 0,
) {

    val vm: UserViewModel = hiltViewModel()

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
        CircularProgressIndicatorForPage()
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
        ErrorMessageForPage()
    }
}