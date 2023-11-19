package cn.bit101.android.ui.gallery.user

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.gallery.common.LoadMoreState
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import cn.bit101.android.ui.gallery.component.AnnotatedText
import cn.bit101.android.ui.gallery.component.PosterCard
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserScreenContent(
    mainController: MainController,
    id: Long?,
    data: GetUserInfoDataModel.Response,
    posters: List<GetPostersDataModel.ResponseItem>,
    state: LoadableLazyColumnWithoutPullRequestState,
    loadState: LoadMoreState?,
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
    val cm = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LoadableLazyColumnWithoutPullRequest(
            modifier = Modifier.fillMaxSize(),
            state = state,
            loading = loadState == LoadMoreState.Loading,
        ) {
            item("avatar") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.align(Alignment.CenterStart)) {
                        Avatar(
                            user = data.user,
                            low = true,
                            size = 80.dp,
                            onClick = { onOpenImage(data.user.avatar) }
                        )
                    }
                    Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                        if(data.own) {
                            if(id == 0L) {
                                FilledTonalButton(onClick = onOpenEditDialog) {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        imageVector = Icons.Rounded.EditNote,
                                        contentDescription = "edit"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        text = "编辑"
                                    )
                                }
                                Spacer(modifier = Modifier.padding(8.dp))
                            }

                            FilledTonalButton(onClick = onSwitchViewPoint) {
                                Icon(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    imageVector = Icons.Rounded.Autorenew,
                                    contentDescription = "edit"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = if(id == null || id == 0L) "去访客视角" else "去个人主页"
                                )
                            }

                        } else if(data.user.id != -1) {
                            FilledTonalButton(
                                onClick = onFollow,
                                enabled = followState !is SimpleState.Loading
                            ) {
                                if(followState is SimpleState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        imageVector = if(data.following) Icons.Rounded.Close else Icons.Rounded.Add,
                                        contentDescription = "add"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        text = if(data.following) "取消关注" else "关注"
                                    )
                                }
                            }
                            if(data.follower && data.following) {
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = "已互粉",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
            item(0) {
                Spacer(modifier = Modifier.padding(8.dp))
            }

            item("nickname") {
                ClickableText(
                    modifier = Modifier.align(Alignment.CenterStart),
                    text = buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ).toSpanStyle()
                        ) {
                            append(data.user.nickname)
                        }

                        val color = if(data.user.identity.id == 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
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
                        mainController.copyText(cm, buildAnnotatedString{ append(data.user.nickname) })
                    }
                )
            }

            item(2) {
                Spacer(modifier = Modifier.padding(4.dp))
            }

            item("info") {
                val time = DateTimeUtils.formatTime(data.user.createTime)
                val timeStr = DateTimeUtils.format(time)
                FlowRow {
                    ClickableText(
                        text = buildAnnotatedString { append("UID: ${data.user.id} | 创建于${timeStr}") },
                        onClick = {
                            mainController.copyText(cm, buildAnnotatedString{ append("${data.user.id}") })
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            item(1) {
                Spacer(modifier = Modifier.padding(8.dp))
            }

            item("motto") {
                SelectionContainer {
                    AnnotatedText(
                        mainController = mainController,
                        text = data.user.motto,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            item(3) {
                Spacer(modifier = Modifier.padding(8.dp))
            }

            item("follow") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                ).toSpanStyle()
                            ) {
                                append("${data.followerNum}")
                            }
                            withStyle(
                                style = MaterialTheme.typography.titleSmall.copy(
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
                    Spacer(modifier = Modifier.padding(12.dp))
                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                ).toSpanStyle()
                            ) {
                                append("${data.followingNum}")
                            }
                            withStyle(
                                style = MaterialTheme.typography.titleSmall.copy(
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
            
            item(10) { 
                Spacer(modifier = Modifier.padding(12.dp))
            }

            posters.forEachIndexed { index, poster ->
                item(index + 100) {
                    PosterCard(
                        data = poster,
                        onOpenPoster = { onOpenPoster(poster.id) },
                        onOpenImages = onOpenImages,
                    )
                }
            }

            item(11) {
                Spacer(modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun UserScreen(
    mainController: MainController,
    onOpenImage: (Image) -> Unit,
    onOpenImages: (Int, List<Image>) -> Unit,
    vm: UserViewModel = hiltViewModel(),
    id: Long = 0,
) {

    val ctx = LocalContext.current

    val getUserInfoState by vm.getUserInfoStateFlow.collectAsState()

    val posters by vm.posterState.dataFlow.collectAsState()

    val postersLoadMoreState by vm.posterState.loadMoreStateFlow.collectAsState()

    val postersRefreshState by vm.posterState.refreshStateFlow.collectAsState()

    val followState by vm.followStateMutableLiveData.observeAsState()

    DisposableEffect(followState) {
        if(followState is SimpleState.Success) {
            vm.followStateMutableLiveData.value = null
            mainController.snackbar("关注/取消关注成功")
        } else if(followState is SimpleState.Fail) {
            vm.followStateMutableLiveData.value = null
            mainController.snackbar("关注/取消关注失败")
        }
        onDispose { }
    }

    var showEditDialog by remember { mutableStateOf(false) }

    var showFollowerDialog by remember { mutableStateOf(false) }

    var showFollowingDialog by remember { mutableStateOf(false) }


    val followers by vm.followersState.dataFlow.collectAsState()
    val refreshFollowersState by vm.followersState.refreshStateFlow.collectAsState()
    val loadMoreFollowersState by vm.followersState.loadMoreStateFlow.collectAsState()

    val followings by vm.followingsState.dataFlow.collectAsState()
    val refreshFollowingsState by vm.followingsState.refreshStateFlow.collectAsState()
    val loadMoreFollowingsState by vm.followingsState.loadMoreStateFlow.collectAsState()


    val uploadAvatarState by vm.uploadAvatarState.observeAsState()

    val userEditData by vm.editUserDataFlow.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
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
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
    }


    val uploadUserInfoState by vm.uploadUserInfoStateLiveData.observeAsState()

    DisposableEffect(uploadUserInfoState) {
        if(uploadUserInfoState is SimpleState.Success) {
            vm.uploadUserInfoStateLiveData.value = null
            mainController.snackbar("保存成功OvO")
        } else if(uploadUserInfoState is SimpleState.Fail) {
            vm.uploadUserInfoStateLiveData.value = null
            mainController.snackbar("保存失败Orz")
        }
        onDispose {  }
    }


    LaunchedEffect(getUserInfoState) {
        if(getUserInfoState == null) {
            vm.getUserInfo(id)
        }
    }

    LaunchedEffect(postersRefreshState) {
        if(postersRefreshState == null) {
            vm.refreshPoster(id)
        }
    }

    when(getUserInfoState) {
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
                    onLoadMore = { vm.loadMorePosters(id) }
                ),
                loadState = postersLoadMoreState,
                followState = followState,

                onOpenImage = onOpenImage,
                onOpenImages = onOpenImages,

                onOpenPoster = { mainController.navController.navigate("poster/$it") },
                onFollow = { vm.follow(id) },
                onOpenEditDialog = { showEditDialog = true },
                onOpenFollowerDialog = { showFollowerDialog = true },
                onOpenFollowingDialog = { showFollowingDialog = true },
                onSwitchViewPoint = {
                    mainController.navController.popBackStack()
                    mainController.navController.navigate("user/${if(id == 0L) data.user.id else 0L}")
                }
            )

            if(showEditDialog) {
                if(id == 0L) {
                    EditUserDialog(
                        user = userEditData ?: data.user,

                        uploadAvatarState = uploadAvatarState,
                        saving = uploadUserInfoState is SimpleState.Loading,

                        onDismiss = { showEditDialog = false },
                        onChange = vm::setUserEditData,
                        onSave = vm::saveUserEditData,
                        onUploadAvatar = { launcher.launch(intent) }
                    )
                } else showEditDialog = false
            }

            if(showFollowerDialog) {
                if(id == 0L) {
                    FollowerDialog(
                        mainController = mainController,
                        followers = followers,
                        refreshState = refreshFollowersState,
                        loadMoreState = loadMoreFollowersState,
                        state = rememberLoadableLazyColumnWithoutPullRequestState(
                            onLoadMore = { vm.loadMoreFollowers() }
                        ),
                        onDismiss = { showFollowerDialog = false },
                        onRefresh = vm::refreshFollowers
                    )
                } else showFollowerDialog = false
            }

            if(showFollowingDialog) {
                if(id == 0L) {
                    FollowingDialog(
                        mainController = mainController,
                        followings = followings,
                        refreshState = refreshFollowingsState,
                        loadMoreState = loadMoreFollowingsState,
                        state = rememberLoadableLazyColumnWithoutPullRequestState(
                            onLoadMore = { vm.loadMoreFollowings() }
                        ),
                        onDismiss = { showFollowingDialog = false },
                        onRefresh = vm::refreshFollowings
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