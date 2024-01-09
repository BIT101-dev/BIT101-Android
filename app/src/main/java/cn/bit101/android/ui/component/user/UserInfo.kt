package cn.bit101.android.ui.component.user

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Identity
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import com.google.gson.Gson

@Composable
private fun AvatarWithName(
    user: User,
    button: @Composable RowScope.() -> Unit = {},
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
            if (user.identity.id == 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Avatar(
                user = user,
                low = true,
                size = 72.dp,
                onClick = { onShowImage(user.avatar) }
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClickableText(
                    text = nicknameText,
                    onClick = { onCopyText(user.nickname) }
                )
                Spacer(modifier = Modifier.padding(4.dp))
                button()
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Icon(
                modifier = Modifier.size(14.dp),
                imageVector = Icons.Rounded.AccountCircle,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                contentDescription = "UID"
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Text(
                text = "UID：${user.id}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.padding(8.dp))

            val timeStr = DateTimeUtils.formatDate(user.createTime)
            if (timeStr != null) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Rounded.DateRange,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    contentDescription = "日期"
                )
                Spacer(modifier = Modifier.padding(3.dp))
                Text(
                    text = "${timeStr}加入",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
private fun FollowInfoItem(
    numberStr: String,
    description: String,
    onClick: () -> Unit = {}
) {
    ClickableText(
        text = buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ).toSpanStyle()
            ) {
                append(numberStr)
            }
            withStyle(
                style = MaterialTheme.typography.bodySmall.copy(
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
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = data.followerNum,
                label = "follower num",
            ) {
                FollowInfoItem(
                    numberStr = it.toString(),
                    description = "粉丝",
                    onClick = onOpenFollowerDialog
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))

            AnimatedContent(
                targetState = data.followingNum,
                label = "following num",
            ) {
                FollowInfoItem(
                    numberStr = it.toString(),
                    description = "关注",
                    onClick = onOpenFollowingDialog
                )
            }
        }
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
    val button: @Composable RowScope.() -> Unit =  {
        val text = if (!data.following) "关注" else if (data.follower) "已互粉" else "已关注"

        val icon = if (!data.following) Icons.Rounded.Add
        else Icons.Rounded.Close

        val color = if(!data.following) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        
        val containerColor = if(!data.following) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    onClick = { onFollow() },
                    enabled = !following
                )
                .background(containerColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = icon,
                    label = "follow icon",
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = it,
                        tint = color,
                        contentDescription = null
                    )
                }

                AnimatedContent(
                    targetState = text,
                    label = "follow",
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }


    Column {
        AvatarWithName(
            user = data.user,
            button = if(data.user.id != -1 && !data.own) button else {{}},
            onShowImage = onShowImage,
            onCopyText = onCopyText,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        SelectionContainer {
            AnnotatedText(
                text = data.user.motto,
                style = MaterialTheme.typography.bodyMedium.copy(
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
    Column {
        AvatarWithName(
            user = data.user,
            onShowImage = onShowImage,
            onCopyText = onCopyText,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        SelectionContainer {
            AnnotatedText(
                text = data.user.motto,
                style = MaterialTheme.typography.bodyMedium.copy(
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