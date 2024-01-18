package cn.bit101.android.ui.component.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.image.PreviewImages
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.android.utils.NumberUtils
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User


internal fun calculateLeftSize(iconSize: Dp) = iconSize * 11 / 10 + 8.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommentCardContent(
    mainController: MainController,
    comment: Comment,
    liking: Boolean,
    isSub: Boolean = false,
    paddingValues: PaddingValues,

    iconSize: Dp,
    leftSize: Dp,

    onClick: () -> Unit,
    onLike: () -> Unit,
    onOpenUserDetail: (User?) -> Unit,
    onOpenImage: (Int) -> Unit,

    onMoreAction: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onClickLabel = "reply",
                onLongClickLabel = "action",
                onLongClick = onMoreAction
            )
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
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Avatar(
                            user = comment.user,
                            low = true,
                            size = iconSize,
                            onClick = { onOpenUserDetail(comment.user) }
                        )
                    }
                }
                Column {
                    val spacePadding = if (isSub) 1.dp else 2.dp
                    val suffixIconSize = 24.dp
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(end = suffixIconSize + 4.dp)) {
                            Text(
                                text = comment.user.nickname,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Spacer(modifier = Modifier.padding(spacePadding))
                            AnnotatedText(
                                text = comment.text,
                                replyUser = if (comment.replyUser.id != 0) comment.replyUser else null,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                onOpenPoster = { mainController.navigate("poster/$it") },
                                onOpenUser = { mainController.navigate("user/$it") }
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
                        Column(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { if (!liking) onLike() })
                                }
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = if(comment.like) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "喜欢",
                                modifier = Modifier
                                    .size(suffixIconSize)
                                    .align(Alignment.CenterHorizontally),
                                tint = if(liking) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else if(comment.like) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurface,
                            )

                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = NumberUtils.format(comment.likeNum),
                                style = MaterialTheme.typography.labelSmall
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
     * 是否显示分割线
     */
    showDivider: Boolean = true,

    /**
     * 正在点赞的评论
     */
    commentLikings: Set<Long>,

    /**
     * 是否显示子评论
     */
    showSubComments: Boolean = true,

    /**
     * 点赞评论
     */
    onLikeComment: (Comment) -> Unit,

    /**
     * 打开图片
     */
    onOpenImage: (Int, List<Image>) -> Unit,

    /**
     * 点击卡片的操作
     */
    onOpenCommentToComment: (Comment, Comment) -> Unit,

    /**
     * 显示更多评论
     */
    onShowMoreComments: () -> Unit = {},

    /**
     * 更多操作
     */
    onMoreAction: (Comment) -> Unit,

) {
    Surface(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
        ) {

            val mainAvatarSize = 36.dp
            val subAvatarSize = 30.dp

            CommentCardContent(
                mainController = mainController,
                comment = comment,
                liking = comment.id.toLong() in commentLikings,
                iconSize = mainAvatarSize,
                leftSize = calculateLeftSize(mainAvatarSize),
                onClick = { onOpenCommentToComment(comment, comment) },
                onLike = { onLikeComment(comment) },
                onOpenUserDetail = { mainController.navigate("user/${comment.user.id}") },
                onOpenImage = { onOpenImage(it, comment.images) },
                paddingValues = PaddingValues(horizontal = 12.dp),
                onMoreAction = { onMoreAction(comment) },
            )

            if (comment.sub.isNotEmpty() && showSubComments) {
                Spacer(modifier = Modifier.padding(2.dp))
                Column(Modifier.padding(start = 45.dp + 12.dp + 2.dp)) {
                    comment.sub.forEach { sub ->
                        CommentCardContent(
                            mainController = mainController,
                            comment = sub,
                            liking = sub.id.toLong() in commentLikings,
                            isSub = true,
                            iconSize = subAvatarSize,
                            leftSize = calculateLeftSize(subAvatarSize),
                            onClick = { onOpenCommentToComment(comment, sub) },
                            onLike = { onLikeComment(sub) },
                            onOpenUserDetail = { mainController.navigate("user/${sub.user.id}") },
                            onOpenImage = { onOpenImage(it, sub.images) },
                            paddingValues = PaddingValues(end = 12.dp),
                            onMoreAction = { onMoreAction(sub) },
                        )
                        Spacer(modifier = Modifier.padding(1.dp))
                    }
                }

                Spacer(modifier = Modifier.padding(2.dp))
                if(comment.sub.size < comment.commentNum) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 45.dp + 12.dp + 0.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onShowMoreComments() })
                                },
                            text = "查看共${comment.commentNum}条回复",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            ),
                        )
                    }
                }
            }

            if(showDivider) {
                Spacer(modifier = Modifier.padding(6.dp))
                Divider(
                    modifier = Modifier.padding(top = 2.dp, bottom = 2.dp, start = calculateLeftSize(mainAvatarSize)),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}