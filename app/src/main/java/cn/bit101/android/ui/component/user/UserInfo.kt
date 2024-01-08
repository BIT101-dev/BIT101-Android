package cn.bit101.android.ui.component.user

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.common.CustomDivider
import cn.bit101.android.ui.component.gallery.AnnotatedText
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

@Composable
fun AvatarWithName(
    mainController: MainController,
    user: User,
    avatarSize: Dp = 54.dp,
    clickable: Boolean = true,
    button: (@Composable () -> Unit)? = null,
) {
    val cm = LocalClipboardManager.current

    val nicknameText = buildAnnotatedString {
        withStyle(
            style = MaterialTheme.typography.titleMedium.copy(
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
            style = MaterialTheme.typography.bodySmall.copy(
                color = color,
                fontWeight = FontWeight.Bold
            ).toSpanStyle()
        ) {
            append(" ${user.identity.text}")
        }
    }

    val uidText = buildAnnotatedString {
        append("UID: ${user.id}")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            user = user,
            low = true,
            size = avatarSize,
            onClick = { mainController.showImage(user.avatar) }
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            if(clickable) {
                ClickableText(
                    text = nicknameText,
                    onClick = {
                        mainController.copyText(cm, buildAnnotatedString { append(user.nickname) })
                    }
                )
                Spacer(modifier = Modifier.padding(2.dp))
                ClickableText(
                    text = uidText,
                    onClick = {
                        mainController.copyText(cm, buildAnnotatedString { append("${user.id}") })
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            } else {
                Text(text = nicknameText,)
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = uidText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

        }
        if (button != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                button()
            }
        }
    }
}

@Composable
private fun FollowInfoItem(
    numberStr: String,
    description: String,
    large: Boolean,
    onClick: () -> Unit = {}
) {
    val numberBaseStyle = if(large) MaterialTheme.typography.titleLarge
    else MaterialTheme.typography.titleMedium

    val descriptionBaseStyle = if(large) MaterialTheme.typography.bodyMedium
    else MaterialTheme.typography.bodySmall

    ClickableText(
        text = buildAnnotatedString {
            withStyle(
                style = numberBaseStyle.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ).toSpanStyle()
            ) {
                append(numberStr)
            }
            withStyle(
                style = descriptionBaseStyle.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.5f
                    )
                ).toSpanStyle()
            ) {
                append(" $description")
            }
        },
        onClick = { onClick() }
    )
}

@Composable
private fun FollowInfo(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    large: Boolean = false,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        FollowInfoItem(
            numberStr = data.followerNum.toString(),
            description = "粉丝",
            large = large,
            onClick = onOpenFollowerDialog
        )
        Spacer(modifier = Modifier.padding(6.dp))
        FollowInfoItem(
            numberStr = data.followingNum.toString(),
            description = "关注",
            large = large,
            onClick = onOpenFollowingDialog
        )
    }
}

@Composable
fun UserInfoContent(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    following: Boolean,
    onFollow: () -> Unit,
) {
    val button = @Composable {
        if (data.user.id != -1 && !data.own) {
            if(data.following) {
                OutlinedButton(
                    modifier = Modifier.padding(0.dp),
                    onClick = onFollow,
                    enabled = !following,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                ) {
                    Text(text = if (data.follower) "已互粉" else "已关注")
                }
            } else {
                FilledTonalButton(
                    modifier = Modifier.padding(0.dp),
                    onClick = onFollow,
                    enabled = !following,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                ) {
                    Text(text = "关注")
                }
            }
        }
    }
    Column {
        AvatarWithName(
            mainController = mainController,
            user = data.user,
            button = button
        )
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
        FollowInfo(
            mainController = mainController,
            data = data,
            onOpenFollowerDialog = {},
            onOpenFollowingDialog = {}
        )
    }
}

@Composable
fun UserInfoContentForMe(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    onOpenMineIndex: () -> Unit,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
    val button = @Composable {
        FilledTonalButton(onClick = onOpenMineIndex) {
            Text(text = "我的主页")
        }
    }
    Column {
        AvatarWithName(
            mainController = mainController,
            user = data.user,
            button = button
        )
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
        FollowInfo(
            mainController = mainController,
            data = data,
            onOpenFollowerDialog = onOpenFollowerDialog,
            onOpenFollowingDialog = onOpenFollowingDialog
        )
    }
}