package cn.bit101.android.features.common.component.user

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.component.gallery.AnnotatedText
import cn.bit101.android.features.common.utils.DateTimeUtils
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

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
            cn.bit101.android.features.common.component.Avatar(
                user = user,
                low = true,
                size = 78.dp,
                onClick = { onShowImage(user.avatar) }
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClickableText(
                    text = nicknameText,
                    onClick = { onCopyText(user.nickname) }
                )
                if(button != {}) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    button()
                }
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Icon(
                modifier = Modifier.size(14.dp),
                imageVector = Icons.Rounded.Numbers,
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
private fun EmptyAvatarWithNickname() {

    val nicknameText = buildAnnotatedString {
        withStyle(
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ).toSpanStyle()
        ) {
            append("空用户名")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            cn.bit101.android.features.common.component.Avatar(
                low = true,
                size = 72.dp,
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = nicknameText)
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                imageVector = Icons.Rounded.Numbers,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                contentDescription = "UID"
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Text(
                text = "UID：-",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            )
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
    followerNum: String,
    followingNum: String,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = followerNum,
                label = "follower num",
            ) {
                FollowInfoItem(
                    numberStr = it,
                    description = "粉丝",
                    onClick = onOpenFollowerDialog
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))

            AnimatedContent(
                targetState = followingNum,
                label = "following num",
            ) {
                FollowInfoItem(
                    numberStr = it,
                    description = "关注",
                    onClick = onOpenFollowingDialog
                )
            }
        }
    }
}

@Composable
private fun FollowInfoWithPoster(
    followerNum: String,
    followingNum: String,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
    onOpenPosterDialog: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = followerNum,
                label = "follower num",
            ) {
                FollowInfoItem(
                    numberStr = it,
                    description = "粉丝",
                    onClick = onOpenFollowerDialog
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))

            AnimatedContent(
                targetState = followingNum,
                label = "following num",
            ) {
                FollowInfoItem(
                    numberStr = it,
                    description = "关注",
                    onClick = onOpenFollowingDialog
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))

            FollowInfoItem(
                numberStr = "?",
                description = "帖子",
                onClick = onOpenPosterDialog
            )
        }
    }
}

@Composable
private fun UserInfoButton(
    text: String,
    icon: ImageVector? = null,
    enable: Boolean,
    color: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = enable
            )
            .background(containerColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = icon,
                label = "follow icon",
            ) {
                if(it != null) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = it,
                        tint = color,
                        contentDescription = null
                    )
                }
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

        UserInfoButton(
            text = text,
            icon = icon,
            enable = !following,
            color = color,
            containerColor = containerColor,
            onClick = onFollow
        )
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
            followerNum = data.followerNum.toString(),
            followingNum = data.followingNum.toString(),
            onOpenFollowerDialog = {},
            onOpenFollowingDialog = {}
        )
    }
}

@Composable
fun UserInfoContentForMe(
    data: GetUserInfoDataModel.Response?,
    onOpenMineIndex: () -> Unit,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
    onOpenPostersDialog: () -> Unit,
    onShowImage: (Image) -> Unit,
    onCopyText: (String) -> Unit,
    onOpenPoster: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
) {
    val button: @Composable RowScope.() -> Unit =  {
        val text = "我的主页"

        val color = MaterialTheme.colorScheme.primary

        val containerColor = MaterialTheme.colorScheme.primaryContainer

        UserInfoButton(
            text = text,
            enable = true,
            color = color,
            containerColor = containerColor,
            onClick = onOpenMineIndex
        )
    }

    Column {
        if(data != null) {
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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    onOpenPoster = onOpenPoster,
                    onOpenUser = onOpenUser
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            FollowInfoWithPoster(
                followerNum = data.followerNum.toString(),
                followingNum = data.followingNum.toString(),
                onOpenFollowerDialog = onOpenFollowerDialog,
                onOpenFollowingDialog = onOpenFollowingDialog,
                onOpenPosterDialog = onOpenPostersDialog
            )
        } else {
            EmptyAvatarWithNickname()
            Spacer(modifier = Modifier.padding(4.dp))
            AnnotatedText(
                text = "空简介",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                onOpenPoster = onOpenPoster,
                onOpenUser = onOpenUser
            )
            Spacer(modifier = Modifier.padding(4.dp))
            FollowInfoWithPoster(
                followerNum = "-",
                followingNum = "-",
                onOpenFollowerDialog = {},
                onOpenFollowingDialog = {},
                onOpenPosterDialog = {}
            )
        }
    }
}