package cn.bit101.android.ui.component.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreHoriz
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.PreviewImages
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.android.utils.NumberUtils
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.User

@Composable
fun CommentCardContent(
    mainController: MainController,
    comment: Comment,
    liking: Boolean,
    isSub: Boolean = false,
    paddingValues: PaddingValues,

    leftSize: Dp,
    iconSize: Dp,

    onClick: () -> Unit,
    onLike: () -> Unit,
    onClickIcon: (User?) -> Unit,
    onOpenImage: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .background(Color.Transparent)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
        ) {
            Row {
                Box(modifier = Modifier.width(leftSize)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    ) {
                        Avatar(
                            user = comment.user,
                            low = true,
                            size = iconSize,
                            onClick = { onClickIcon(comment.user) }
                        )
                    }
                }
                Column {
                    val spacePadding = if (isSub) 1.dp else 2.dp

                    Spacer(modifier = Modifier.padding(spacePadding))
                    Box(modifier = Modifier.fillMaxWidth(),) {
                        Text(
                            modifier = Modifier.padding(end = 22.dp),
                            text = comment.user.nickname,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.CenterEnd),
                            onClick = { /*TODO*/ },
                        ) {
                            Icon(imageVector = Icons.Rounded.MoreHoriz, contentDescription = "更多")
                        }
                    }

                    Spacer(modifier = Modifier.padding(spacePadding))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(end = 22.dp)) {
                            AnnotatedText(
                                mainController = mainController,
                                text = comment.text,
                                replyUser = if (comment.replyUser.id != 0) comment.replyUser else null,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.padding(spacePadding))
                            // 图片
                            if (comment.images.isNotEmpty()) {
                                PreviewImages(
                                    size = if (isSub) 80.dp else 100.dp,
                                    images = comment.images,
                                    onClick = { onOpenImage(it) },
                                )
                                Spacer(modifier = Modifier.padding(spacePadding))
                            }

                            val time = DateTimeUtils.formatTime(comment.updateTime)
                            val diff = if(time == null) "未知"
                            else DateTimeUtils.calculateTimeDiff(time)

                            Text(
                                text = diff,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ),
                            )
                        }
                        Column(modifier = Modifier.align(Alignment.TopEnd)) {
                            IconButton(
                                modifier = Modifier.size(18.dp).align(Alignment.CenterHorizontally),
                                colors = IconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = if(comment.like) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                ),
                                onClick = onLike,
                                enabled = !liking,
                            ) {
                                Icon(
                                    imageVector = if(comment.like) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "喜欢"
                                )
                            }
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = NumberUtils.format(comment.likeNum),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    mainController: MainController,

    /**
     * 评论
     */
    comment: Comment,

    /**
     * 正在点赞的评论
     */
    commentLikings: Set<Long>,

    /**
     * 是否显示子评论
     */
    showSubComments: Boolean = true,

    /**
     * 卡片颜色
     */
    colors: CardColors = CardDefaults.cardColors(),

    /**
     * 点赞评论
     */
    onLikeComment: (Long) -> Unit,

    /**
     * 打开图片
     */
    onOpenImage: (Int) -> Unit,

    /**
     * 点击卡片的操作
     */
    onClick: () -> Unit,

    /**
     * 显示更多评论
     */
    onShowMoreComments: () -> Unit = {},

    /**
     * 打开删除评论的对话框
     */
    onOpenDeleteCommentDialog: () -> Unit,

    /**
     * 举报评论
     */
    onReport: () -> Unit,
) {
    val ctx = LocalContext.current
    val cm = LocalClipboardManager.current
    var menuState by remember(comment.id) { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .background(colors.containerColor)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
        ) {

            CommentCardContent(
                mainController = mainController,
                comment = comment,
                liking = comment.id.toLong() in commentLikings,
                leftSize = 45.dp,
                iconSize = 35.dp,
                onClick = onClick,
                onLike = { onLikeComment(comment.id.toLong()) },
                onClickIcon = { mainController.navController.navigate("user/${comment.user.id}") },
                onOpenImage = onOpenImage,
                paddingValues = PaddingValues(horizontal = 8.dp),
            )

            if (comment.sub.isNotEmpty() && showSubComments) {
                Spacer(modifier = Modifier.padding(2.dp))
                Column(Modifier.padding(start = 45.dp + 8.dp)) {
                    comment.sub.forEach { sub ->
                        CommentCardContent(
                            mainController = mainController,
                            comment = sub,
                            liking = sub.id.toLong() in commentLikings,
                            isSub = true,
                            leftSize = 40.dp,
                            iconSize = 30.dp,
                            onClick = onClick,
                            onLike = { onLikeComment(sub.id.toLong()) },
                            onClickIcon = { mainController.navController.navigate("user/${comment.sub[0].user.id}") },
                            onOpenImage = onOpenImage,
                            paddingValues = PaddingValues(end = 8.dp),
                        )
                        Spacer(modifier = Modifier.padding(1.dp))
                    }
                }

                Spacer(modifier = Modifier.padding(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 45.dp + 8.dp + 0.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clickable { onShowMoreComments() },
                        text = "查看共${comment.commentNum}条评论",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }
            }
        }
    }
}