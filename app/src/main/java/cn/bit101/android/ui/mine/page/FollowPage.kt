package cn.bit101.android.ui.mine.page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.common.ErrorMessageForPage
import cn.bit101.android.ui.component.loadable.LoadableLazyColumn
import cn.bit101.android.ui.component.loadable.LoadableLazyColumnState
import cn.bit101.android.ui.component.user.AvatarWithName
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
            AvatarWithName(
                mainController = mainController,
                user = user,
                avatarSize = 46.dp,
                clickable = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowPage(
    mainController: MainController,

    title: String,
    users: List<User>,
    state: LoadableLazyColumnState,
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

    BackHandler {
        onDismiss()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if(refreshState is SimpleState.Fail) ErrorMessageForPage()
            else if(refreshState is SimpleState.Success) {
                LoadableLazyColumn(
                    state = state,
                    loading = loadMoreState is SimpleState.Loading,
                    refreshing = refreshState is SimpleState.Loading,
                ) {
                    items(users) {
                        UserItem(
                            mainController = mainController,
                            user = it,
                            onClick = { mainController.navigate("user/${it.id}") }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun FollowerPage(
    mainController: MainController,

    followers: List<User>,
    state: LoadableLazyColumnState,
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
    state: LoadableLazyColumnState,
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