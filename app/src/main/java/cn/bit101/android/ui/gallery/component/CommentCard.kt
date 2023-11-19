package cn.bit101.android.ui.gallery.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.PreviewImages
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Comment

@Composable
fun CommentCard(
    mainController: MainController,

    comment: Comment,
    commentLikes: Set<Long>,
    showSubComments: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(),

    onLikeComment: () -> Unit,
    onOpenImage: (Int) -> Unit,
    onClick: () -> Unit,
    onShowMoreComments: () -> Unit = {},
    onOpenDeleteCommentDialog: () -> Unit,
    onReport: () -> Unit,
) {
    val ctx = LocalContext.current
    val cm = LocalClipboardManager.current
    var menuState by remember(comment.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        onClick = onClick,
        colors = colors,
    ) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(
                        user = comment.user,
                        low = true,
                        size = 45.dp,
                        onClick = { mainController.navController.navigate("user/${comment.user.id}") }
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp),
                    ) {
                        Text(
                            text = comment.user.nickname,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.padding(vertical = 1.dp))
                        val time = DateTimeUtils.format(DateTimeUtils.formatTime(comment.updateTime))

                        Text(
                            text = time ?: "获取失败了捏",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 点赞
                    Box {
                        val liking = commentLikes.contains(comment.id.toLong())
                        if(comment.like) {
                            IconButton(
                                onClick = onLikeComment,
                                colors = IconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = MaterialTheme.colorScheme.tertiary,
                                ),
                                enabled = !liking
                            ) {
                                if(liking) {
                                    CircularProgressIndicator()
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.ThumbUp,
                                        contentDescription = "点赞",
                                    )
                                }
                            }

                        } else {
                            IconButton(
                                onClick = onLikeComment,
                                enabled = !liking
                            ) {
                                if(liking) {
                                    CircularProgressIndicator()
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.ThumbUp,
                                        contentDescription = "点赞",
                                    )
                                }
                            }
                        }


                        if(comment.likeNum > 0) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(text = comment.likeNum.toString())
                            }
                        }
                    }

                    // 菜单
                    IconButton(
                        onClick = { menuState = !menuState }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "",
                        )
                    }
                    DropdownMenu(
                        expanded = menuState,
                        onDismissRequest = { menuState = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("复制") },
                            onClick = { mainController.copyText(cm, buildAnnotatedString { append(comment.text) }) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "复制"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("举报") },
                            onClick = onReport,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Error,
                                    contentDescription = "举报"
                                )
                            }
                        )
                        if(comment.own) {
                            DropdownMenuItem(
                                text = { Text("删除") },
                                onClick = onOpenDeleteCommentDialog,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "删除"
                                    )
                                }
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.padding(vertical = 6.dp))

            if(comment.text.isNotEmpty()) {
                AnnotatedText(
                    mainController = mainController,
                    text = comment.text,
                    replyUser = if(comment.replyUser.id != 0) comment.replyUser else null,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.padding(vertical = 4.dp))
            }

            // 图片
            if(comment.images.isNotEmpty()) {
                PreviewImages(
                    images = comment.images,
                    onClick = { onOpenImage(it) },
                )
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
            }
            // 子评论
            if(comment.sub.isNotEmpty() && showSubComments) {
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    comment.sub.forEach { sub ->
                        val text = "${sub.user.nickname}： " + if(sub.replyUser.id != 0) {
                            "@${sub.replyUser.nickname} "
                        } else {
                            ""
                        } + sub.text.replace("\n", " ") + "[图片]".repeat(sub.images.size)

                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onShowMoreComments),
                        text = "共${comment.commentNum}条回复 >>",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}