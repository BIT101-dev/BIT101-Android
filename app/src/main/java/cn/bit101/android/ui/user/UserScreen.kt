package cn.bit101.android.ui.user

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.ImagePicker
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.gallery.AnnotatedText
import cn.bit101.android.ui.component.gallery.PosterCard
import cn.bit101.android.ui.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.loadable.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.topbar.BasicTwoRowsTopAppBar
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserInfoContent(
    mainController: MainController,
    id: Long?,
    data: GetUserInfoDataModel.Response,
    followState: SimpleState?,

    onFollow: () -> Unit,
    onSwitchViewPoint: () -> Unit,

    onOpenEditDialog: () -> Unit,
    onOpenImage: (Image) -> Unit,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
    val cm = LocalClipboardManager.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                user = data.user,
                low = true,
                size = 56.dp,
                onClick = { onOpenImage(data.user.avatar) }
            )
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                horizontalAlignment = Alignment.End
//            ) {
//                if (data.user.id != -1) {
//                    FilledTonalButton(
//                        onClick = onFollow,
//                        enabled = followState !is SimpleState.Loading
//                    ) {
//                        if (followState is SimpleState.Loading) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(20.dp),
//                                strokeWidth = 2.dp
//                            )
//                        } else {
//                            Icon(
//                                modifier = Modifier.align(Alignment.CenterVertically),
//                                imageVector = if (data.following) Icons.Rounded.Close else Icons.Rounded.Add,
//                                contentDescription = "add"
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text(
//                                modifier = Modifier.align(Alignment.CenterVertically),
//                                text = if (data.following) {
//                                    if (data.follower) "已互粉" else "已关注"
//                                } else "关注"
//                            )
//                        }
//                    }
//                }
//            }
            Spacer(modifier = Modifier.padding(8.dp))
            Column {
                ClickableText(
                    text = buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ).toSpanStyle()
                        ) {
                            append(data.user.nickname)
                        }

                        val color =
                            if (data.user.identity.id == 0) MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.5f
                            )
                            else Color(android.graphics.Color.parseColor(data.user.identity.color))

                        withStyle(
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = color,
                                fontWeight = FontWeight.Bold
                            ).toSpanStyle()
                        ) {
                            append(" ${data.user.identity.text}")
                        }
                    },
                    onClick = {
                        mainController.copyText(cm, buildAnnotatedString { append(data.user.nickname) })
                    }
                )
                Spacer(modifier = Modifier.padding(2.dp))
                ClickableText(
                    text = buildAnnotatedString { append("UID: ${data.user.id}") },
                    onClick = {
                        mainController.copyText(cm, buildAnnotatedString { append("${data.user.id}") })
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))
        SelectionContainer {
            AnnotatedText(
                mainController = mainController,
                text = data.user.motto,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ).toSpanStyle()
                    ) {
                        append("${data.followerNum}")
                    }
                    withStyle(
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.5f
                            )
                        ).toSpanStyle()
                    ) {
                        append(" 粉丝")
                    }
                },
                onClick = { onOpenFollowerDialog() }
            )
            Spacer(modifier = Modifier.padding(6.dp))
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ).toSpanStyle()
                    ) {
                        append("${data.followingNum}")
                    }
                    withStyle(
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.5f
                            )
                        ).toSpanStyle()
                    ) {
                        append(" 关注")
                    }
                },
                onClick = { onOpenFollowingDialog() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreenContent(
    mainController: MainController,
    id: Long?,
    data: GetUserInfoDataModel.Response,
    posters: List<GetPostersDataModel.ResponseItem>,
    state: LoadableLazyColumnWithoutPullRequestState,
    loadState: SimpleState?,
    followState: SimpleState?,

    onFollow: () -> Unit,
    onSwitchViewPoint: () -> Unit,

    onOpenEditDialog: () -> Unit,
    onOpenPoster: (Long) -> Unit,
    onOpenImage: (Image) -> Unit,
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
                        id = id,
                        data = data,
                        followState = followState,

                        onFollow = onFollow,
                        onSwitchViewPoint = onSwitchViewPoint,

                        onOpenEditDialog = onOpenEditDialog,
                        onOpenImage = onOpenImage,
                        onOpenFollowerDialog = onOpenFollowerDialog,
                        onOpenFollowingDialog = onOpenFollowingDialog,
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

    val postersLoadMoreState by vm.posterStateExport.loadMoreStateFlow.collectAsState()

    val postersRefreshState by vm.posterStateExport.refreshStateFlow.collectAsState()

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

    val imagePicker = ImagePicker(
        launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val uri = data.data
                    if (uri != null) {
                        vm.uploadAvatar(ctx, uri)
                    }
                }
            }
        }
    )


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

    when (getUserInfoState) {
        null, is SimpleDataState.Loading -> {
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
        }

        is SimpleDataState.Success -> {
            val data = (getUserInfoState as SimpleDataState.Success).data

            UserScreenContent(
                mainController = mainController,
                id = id,
                data = data,
                posters = posters,

                state = rememberLoadableLazyColumnWithoutPullRequestState(
                    onLoadMore = { vm.posterStateExport.loadMore(id) }
                ),
                loadState = postersLoadMoreState,
                followState = followState,

                onOpenImage = mainController::showImage,
                onOpenImages = mainController::showImages,

                onOpenPoster = { mainController.navController.navigate("poster/$it") },
                onFollow = { vm.follow(id) },
                onOpenEditDialog = { showEditDialog = true },
                onOpenFollowerDialog = { showFollowerDialog = true },
                onOpenFollowingDialog = { showFollowingDialog = true },
                onSwitchViewPoint = {
                    mainController.navController.popBackStack()
                    mainController.navController.navigate("user/${if (id == 0L) data.user.id else 0L}")
                }
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

        }

        is SimpleDataState.Fail -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "加载失败",
                    textAlign = TextAlign.Center
                )
            }
        }
    }

}