package cn.bit101.android.ui.component.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.gallery.AnnotatedText
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel


@Composable
private fun BasicUserInfoContent(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    button: (@Composable () -> Unit)? = null,
    onOpenFollowerDialog: () -> Unit = {},
    onOpenFollowingDialog: () -> Unit = {},
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
                size = 54.dp,
                onClick = { mainController.showImage(data.user.avatar) }
            )
            Spacer(modifier = Modifier.padding(4.dp))
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
            if (button != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    button()
                }
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


@Composable
fun UserInfoContent(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    following: Boolean,

    onFollow: () -> Unit,
) {

    BasicUserInfoContent(
        mainController = mainController,
        data = data,
        button = {
            if (data.user.id != -1) {
                if(data.following) {
                    OutlinedButton(
                        onClick = onFollow,
                        enabled = !following,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                    ) {
                        Text(text = if (data.follower) "已互粉" else "已关注")
                    }
                } else {
                    FilledTonalButton(
                        onClick = onFollow,
                        enabled = !following,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                    ) {
                        Text(text = "关注")
                    }
                }

            }
        },
    )
}


@Composable
fun UserInfoContentForMe(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
) {
    BasicUserInfoContent(
        mainController = mainController,
        data = data,
        onOpenFollowerDialog = onOpenFollowerDialog,
        onOpenFollowingDialog = onOpenFollowingDialog,
    )
}