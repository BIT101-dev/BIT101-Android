package cn.bit101.android.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.UploadImageState
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.loadable.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.api.model.common.User

@Composable
fun EditUserDialog(
    user: User,
    uploadAvatarState: UploadImageState?,

    saving: Boolean,

    onDismiss: () -> Unit,
    onUploadAvatar: () -> Unit,
    onChange: (User) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "编辑个人信息") },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave()
                    onDismiss()
                },
                enabled = uploadAvatarState !is UploadImageState.Loading && !saving
            ) {
                Text(text = "保存")
            }
        },
        text = {
            Column {
                Text(text = "头像", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(4.dp))
                Avatar(
                    user = user,
                    low = false,
                    size = 60.dp,
                    showIdentity = false,
                    onClick = { onUploadAvatar() }
                )

                Spacer(modifier = Modifier.padding(10.dp))

                Text(text = "昵称", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = user.nickname,
                    onValueChange = { onChange(user.copy(nickname = it)) },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )

                Spacer(modifier = Modifier.padding(10.dp))

                Text(text = "个性签名", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = user.motto,
                    onValueChange = { onChange(user.copy(motto = it)) },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    maxLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}


@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                user = user,
                low = false,
                size = 50.dp,
            )
            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "UID: ${user.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }
        }
    }
}


@Composable
fun FollowerDialog(
    mainController: MainController,

    followers: List<User>,

    state: LoadableLazyColumnWithoutPullRequestState,
    refreshState: SimpleState?,
    loadMoreState: SimpleState?,

    onRefresh: () -> Unit,

    onDismiss: () -> Unit,
) {
    LaunchedEffect(refreshState) {
        if(refreshState == null) {
            onRefresh()
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.6f),
        onDismissRequest = onDismiss,
        title = { Text(text = "粉丝") },
        dismissButton = {},
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "确定")
            }
        },
        text = {
            LoadableLazyColumnWithoutPullRequest(
                modifier = Modifier.fillMaxWidth(0.9f),
                state = state,
                loading = loadMoreState is SimpleState.Loading,
            ) {
                items(followers) {
                    UserCard(
                        user = it,
                        onClick = {
                            onDismiss()
                            mainController.navController.navigate("user/${it.id}")
                        }
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                }
            }
        }
    )
}


@Composable
fun FollowingDialog(
    mainController: MainController,

    followings: List<User>,
    state: LoadableLazyColumnWithoutPullRequestState,
    refreshState: SimpleState?,
    loadMoreState: SimpleState?,

    onRefresh: () -> Unit,

    onDismiss: () -> Unit,
) {
    LaunchedEffect(refreshState) {
        if(refreshState == null) {
            onRefresh()
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.6f),
        onDismissRequest = onDismiss,
        title = { Text(text = "关注") },
        dismissButton = {},
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "确定")
            }
        },
        text = {
            LoadableLazyColumnWithoutPullRequest(
                modifier = Modifier.fillMaxWidth(0.9f),
                state = state,
                loading = loadMoreState is SimpleState.Loading,
            ) {
                items(followings) {
                    UserCard(
                        user = it,
                        onClick = {
                            onDismiss()
                            mainController.navController.navigate("user/${it.id}")
                        }
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                }
            }
        }
    )
}