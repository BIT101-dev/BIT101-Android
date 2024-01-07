package cn.bit101.android.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
    val topAppBarBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null,
    )

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarBehavior.nestedScrollConnection),
        topBar = {
            BasicTwoRowsTopAppBar(
                title = {
                    UserInfoContent(
                        mainController = mainController,
                        data = data,
                        following = followState is SimpleState.Loading,
                        onFollow = onFollow,
                    )
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
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
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

    var showEditDialog by remember { mutableStateOf(false) }

    var showFollowerDialog by remember { mutableStateOf(false) }

    var showFollowingDialog by remember { mutableStateOf(false) }


    val followers by vm.followersStateExport.dataFlow.collectAsState()
    val refreshFollowersState by vm.followersStateExport.refreshStateFlow.collectAsState()
    val loadMoreFollowersState by vm.followersStateExport.loadMoreStateFlow.collectAsState()

    val followings by vm.followingsStateExport.dataFlow.collectAsState()
    val refreshFollowingsState by vm.followingsStateExport.refreshStateFlow.collectAsState()
    val loadMoreFollowingsState by vm.followingsStateExport.loadMoreStateFlow.collectAsState()

    val uploadAvatarState by vm.uploadAvatarState.observeAsState()

    val userEditData by vm.editUserDataFlow.collectAsState()

    val imagePicker = rememberImagePicker {
        vm.uploadAvatar(ctx, it)
    }


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
        val data = (getUserInfoState as SimpleDataState.Success).data

        UserScreenContent(
            mainController = mainController,
            data = data,
            posters = posters,

            state = rememberLoadableLazyColumnWithoutPullRequestState(
                onLoadMore = { vm.posterStateExport.loadMore(id) }
            ),
            loadState = postersLoadMoreState,
            followState = followState,

            onOpenImages = mainController::showImages,
            onOpenPoster = { mainController.navController.navigate("poster/$it") },
            onFollow = { vm.follow(id) },
            onOpenFollowerDialog = { showFollowerDialog = true },
            onOpenFollowingDialog = { showFollowingDialog = true },
        )

        if (showEditDialog) {
            if (id == 0L) {
                EditUserDialog(
                    user = userEditData ?: data.user,

                    uploadAvatarState = uploadAvatarState,
                    saving = uploadUserInfoState is SimpleState.Loading,

                    onDismiss = { showEditDialog = false },
                    onChange = vm::setUserEditData,
                    onSave = vm::saveUserEditData,
                    onUploadAvatar = imagePicker::pickImage
                )
            } else showEditDialog = false
        }

        if (showFollowerDialog) {
            if (id == 0L) {
                FollowerDialog(
                    mainController = mainController,
                    followers = followers,
                    refreshState = refreshFollowersState,
                    loadMoreState = loadMoreFollowersState,
                    state = rememberLoadableLazyColumnWithoutPullRequestState(
                        onLoadMore = vm.followersStateExport.loadMore
                    ),
                    onDismiss = { showFollowerDialog = false },
                    onRefresh = vm.followersStateExport.refresh
                )
            } else showFollowerDialog = false
        }

        if (showFollowingDialog) {
            if (id == 0L) {
                FollowingDialog(
                    mainController = mainController,
                    followings = followings,
                    refreshState = refreshFollowingsState,
                    loadMoreState = loadMoreFollowingsState,
                    state = rememberLoadableLazyColumnWithoutPullRequestState(
                        onLoadMore = vm.followingsStateExport.loadMore
                    ),
                    onDismiss = { showFollowingDialog = false },
                    onRefresh = vm.followingsStateExport.refresh
                )
            } else showFollowingDialog = false
        }
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