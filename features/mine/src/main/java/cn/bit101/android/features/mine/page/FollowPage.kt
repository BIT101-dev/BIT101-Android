package cn.bit101.android.features.mine.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.Avatar
import cn.bit101.android.features.common.component.ErrorMessageForPage
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.api.model.common.User


@Composable
private fun UserItem(
    mainController: MainController,
    user: User,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            val nicknameText = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ).toSpanStyle()
                ) {
                    append(user.nickname)
                }

                val color =
                    if (user.identity.id == 0) MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.5f
                    )
                    else Color(android.graphics.Color.parseColor(user.identity.color))

                withStyle(
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Bold
                    ).toSpanStyle()
                ) {
                    append(" ${user.identity.text}")
                }
            }

            val uidText = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ).toSpanStyle()
                ) {
                    append("UID：${user.id}")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    user = user,
                    low = true,
                    size = 46.dp,
                )
                Spacer(modifier = Modifier.padding(6.dp))
                Column {
                    Text(text = nicknameText)
                    Text(text = uidText)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowPage(
    mainController: MainController,

    title: String,
    users: List<User>,
    state: LoadableLazyColumnWithoutPullRequestState,
    refreshState: SimpleState?,
    loadMoreState: SimpleState?,

    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onClearState: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if(refreshState == null) {
            onRefresh()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onClearState()
        }
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
                        text = title,
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
        Box(modifier = Modifier.padding(paddingValues)) {
            if(refreshState is SimpleState.Fail) ErrorMessageForPage()
            else if(refreshState is SimpleState.Success) {
                LoadableLazyColumnWithoutPullRequest(
                    state = state,
                    loading = loadMoreState is SimpleState.Loading,
                ) {
                    items(users) {
                        UserItem(
                            mainController = mainController,
                            user = it,
                            onClick = { mainController.navigate(NavDest.User(it.id.toLong())) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
internal fun FollowerPage(
    mainController: MainController,

    followers: List<User>,
    state: LoadableLazyColumnWithoutPullRequestState,
    refreshState: SimpleState?,
    loadMoreState: SimpleState?,

    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onClearState: () -> Unit,
) {
    FollowPage(
        mainController = mainController,
        title = "我的粉丝",
        users = followers,
        state = state,
        refreshState = refreshState,
        loadMoreState = loadMoreState,
        onRefresh = onRefresh,
        onDismiss = onDismiss,
        onClearState = onClearState,
    )
}

@Composable
fun FollowingPage(
    mainController: MainController,

    followings: List<User>,
    state: LoadableLazyColumnWithoutPullRequestState,
    refreshState: SimpleState?,
    loadMoreState: SimpleState?,

    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onClearState: () -> Unit,
) {
    FollowPage(
        mainController = mainController,
        title = "我的关注",
        users = followings,
        state = state,
        refreshState = refreshState,
        loadMoreState = loadMoreState,
        onRefresh = onRefresh,
        onDismiss = onDismiss,
        onClearState = onClearState,
    )
}