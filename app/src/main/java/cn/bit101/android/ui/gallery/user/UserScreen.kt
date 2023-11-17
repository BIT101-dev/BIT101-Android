package cn.bit101.android.ui.gallery.user

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
import androidx.compose.material.ExperimentalMaterialApi
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
import cn.bit101.android.ui.gallery.component.PosterCard
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import java.net.URLEncoder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserScreenPage(
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
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
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

                            FilledTonalButton(onClick = onSwitchViewPoint) {
                                Icon(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    imageVector = Icons.Rounded.Autorenew,
                                    contentDescription = "edit"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    text = if(id == null || id == 0L) "去访客视角" else "去个人视角"
                                )
                            }

                        } else {
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
                        }
                    }
                }
            }
            item(0) {
                Spacer(modifier = Modifier.padding(8.dp))
            }

            item("nickname") {
                Text(
                    modifier = Modifier.align(Alignment.CenterStart),
                    text = buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
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
                )
            }

            item(2) {
                Spacer(modifier = Modifier.padding(4.dp))
            }

            item("info") {
                val time = DateTimeUtils.formatTime(data.user.createTime)
                val timeStr = DateTimeUtils.format(time)
                FlowRow {
                    Text(
                        text = "UID: ${data.user.id} | 创建于${timeStr}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            item(1) {
                Spacer(modifier = Modifier.padding(8.dp))
            }

            item("motto") {
                Text(
                    text = data.user.motto,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                                    fontWeight = FontWeight.Bold
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
                                    fontWeight = FontWeight.Bold
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
                        onOpenPoster = onOpenPoster,
                    )
                }
            }

            item(11) {
                Spacer(modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserScreen(
    mainController: MainController,
    vm: UserViewModel = hiltViewModel(),
    id: Long = 0,
) {

    val getUserInfoState by vm.getUserInfoStateFlow.collectAsState()

    val posters by vm.posterState.dataFlow.collectAsState()

    val postersLoadMoreState by vm.posterState.loadMoreStateFlow.collectAsState()

    val postersRefreshState by vm.posterState.refreshStateFlow.collectAsState()

    val state = rememberLoadableLazyColumnWithoutPullRequestState(
        onLoadMore = { vm.loadMorePosters(id) }
    )

    val followState by vm.followStateMutableLiveData.observeAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    var showFollowerDialog by remember { mutableStateOf(false) }

    var showFollowingDialog by remember { mutableStateOf(false) }

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

            UserScreenPage(
                id = id,
                data = data,
                posters = posters,

                state = state,
                loadState = postersLoadMoreState,
                followState = followState,

                onOpenImage = {
                    val encodedUrl = URLEncoder.encode(it.url, "UTF-8")
                    mainController.navController.navigate("image/$encodedUrl")
                },

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
                if(data.own) {
                    EditUserDialog(
                        data = data,
                        onDismiss = { showEditDialog = false },
                        onSave = { avatar, nickname, motto ->

                        }
                    )
                } else showEditDialog = false
            }

            if(showFollowerDialog) {
                if(data.own) {
                    FollowerDialog(
                        mainController = mainController,
                        data = data,
                        onDismiss = { showFollowerDialog = false },
                    )
                } else showFollowerDialog = false
            }

            if(showFollowingDialog) {
                if(data.own) {
                    FollowingDialog(
                        mainController = mainController,
                        data = data,
                        onDismiss = { showFollowingDialog = false },
                    )
                } else showFollowingDialog = false
            }

        }
        is SimpleDataState.Error -> {
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