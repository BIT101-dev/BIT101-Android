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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.common.CustomDivider
import cn.bit101.android.ui.component.gallery.AnnotatedText
import cn.bit101.api.model.common.Identity
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import com.google.gson.Gson

@Composable
fun AvatarWithName(
    user: User,
    avatarSize: Dp = 54.dp,
    clickable: Boolean = true,
    button: (@Composable () -> Unit)? = null,
    onShowImage: (Image) -> Unit,
    onCopyText: (String) -> Unit,
) {
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
            onClick = { onShowImage(user.avatar) }
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            if(clickable) {
                ClickableText(
                    text = nicknameText,
                    onClick = { onCopyText(user.nickname) }
                )
                Spacer(modifier = Modifier.padding(2.dp))
                ClickableText(
                    text = uidText,
                    onClick = { onCopyText("${user.id}") },
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
    data: GetUserInfoDataModel.Response,
    following: Boolean,
    onFollow: () -> Unit,
    onShowImage: (Image) -> Unit,
    onCopyText: (String) -> Unit,
    onOpenPoster: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
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
            user = data.user,
            button = button,
            onShowImage = onShowImage,
            onCopyText = onCopyText,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        SelectionContainer {
            AnnotatedText(
                text = data.user.motto,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                onOpenPoster = onOpenPoster,
                onOpenUser = onOpenUser,
            )
        }
        Spacer(modifier = Modifier.padding(4.dp))
        FollowInfo(
            data = data,
            onOpenFollowerDialog = {},
            onOpenFollowingDialog = {}
        )
    }
}

@Composable
fun UserInfoContentForMe(
    data: GetUserInfoDataModel.Response,
    onOpenMineIndex: () -> Unit,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
    onShowImage: (Image) -> Unit,
    onCopyText: (String) -> Unit,
    onOpenPoster: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
) {
    val button = @Composable {
        FilledTonalButton(onClick = onOpenMineIndex) {
            Text(text = "我的主页")
        }
    }
    Column {
        AvatarWithName(
            user = data.user,
            button = button,
            onShowImage = onShowImage,
            onCopyText = onCopyText,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        SelectionContainer {
            AnnotatedText(
                text = data.user.motto,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                onOpenPoster = onOpenPoster,
                onOpenUser = onOpenUser
            )
        }
        Spacer(modifier = Modifier.padding(4.dp))
        FollowInfo(
            data = data,
            onOpenFollowerDialog = onOpenFollowerDialog,
            onOpenFollowingDialog = onOpenFollowingDialog
        )
    }
}


@Preview
@Composable
private fun PreviewUserInfo() {
    val userJson = """
        {
            "user": {
                "id": 4146,
                "create_time": "2023-11-02T01:34:06.991293+08:00",
                "nickname": "教务",
                "avatar": {
                    "mid": "",
                    "url": "https://bit101-1255944436.cos.ap-beijing.myqcloud.com/img/e2e4437695e019484769bc807948dad8.jpeg",
                    "low_url": "https://bit101-1255944436.cos.ap-beijing.myqcloud.com/img/e2e4437695e019484769bc807948dad8.jpeg!low"
                },
                "motto": "教务部、教务处",
                "identity": {
                    "id": 6,
                    "create_time": "2023-10-31T01:08:07.611437+08:00",
                    "update_time": "2023-10-31T01:08:07.611437+08:00",
                    "delete_time": null,
                    "text": "机器人",
                    "color": "#8350EB"
                }
            },
            "following_num": 0,
            "follower_num": 15,
            "following": false,
            "follower": false,
            "own": false
        }
    """.trimIndent()
    val res = Gson().fromJson(userJson, GetUserInfoDataModel.Response::class.java)
    Surface {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            UserInfoContent(
                data = res,
                following = false,
                onFollow = {},
                onCopyText = {},
                onShowImage = {},
                onOpenPoster = {},
                onOpenUser = {},
            )
        }
    }
}