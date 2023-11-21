package cn.bit101.android.ui.gallery.poster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.gallery.CommentCard
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image


@Composable
fun MoreCommentsSheetContent(
    mainController: MainController,

    /**
     * 主评论
     */
    comment: Comment,

    /**
     * 所有子评论
     */
    subComments: List<Comment>,

    /**
     * 所有评论的点赞状态，存储在一个Set中，如果评论的id在Set中，说明正在进行点赞操作
     */
    commentLikings: Set<Long>,

    /**
     * 是否正在*加载更多*评论
     */
    loading: Boolean,

    /**
     * 是否正在刷新评论
     */
    refreshing: Boolean,

    /**
     * 评论列表的状态，这里依然没有下拉刷新这个操作
     */
    state: LoadableLazyColumnWithoutPullRequestState,

    /**
     * 点赞评论
     */
    onLikeComment: (Long) -> Unit,

    /**
     * 打开图片组
     */
    onOpenImages: (Int, List<Image>) -> Unit,

    /**
     * 打开*对评论的评论*的编辑框，第一个是主评论，第二个是回复的评论
     */
    onOpenCommentDialog: (Comment, Comment) -> Unit,

    /**
     * 举报评论
     */
    onReport: (Comment) -> Unit,

    /**
     * 打开删除评论的对话框
     */
    onOpenDeleteCommentDialog: (Comment) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .nestedScroll(rememberNestedScrollInteropConnection()),
    ) {
        LoadableLazyColumnWithoutPullRequest(
            loading = loading,
            state = state,
        ) {
            item(comment.id) {
                CommentCard(
                    mainController = mainController,
                    comment = comment,
                    onOpenImage = { onOpenImages(it, comment.images) },
                    showSubComments = false,
                    commentLikings = commentLikings,
                    onLikeComment = {
                        onLikeComment(comment.id.toLong())
                    },
                    onClick = {
                        // 第一个是主评论，第二个是回复的评论
                        onOpenCommentDialog(comment, comment)
                    },
                    onReport = { onReport(comment) },
                    onOpenDeleteCommentDialog = { onOpenDeleteCommentDialog(comment) },
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = "以下是该评论的回复",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if(refreshing) {
                item("refreshing") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                subComments.forEach { sub ->
                    item(sub.id + 40) {
                        CommentCard(
                            mainController = mainController,
                            comment = sub,
                            onOpenImage = { onOpenImages(it, sub.images) },
                            showSubComments = false,
                            commentLikings = commentLikings,
                            onLikeComment = { onLikeComment(sub.id.toLong()) },
                            onClick = { onOpenCommentDialog(comment, sub) },
                            onReport = { onReport(sub) },
                            onOpenDeleteCommentDialog = { onOpenDeleteCommentDialog(sub) },
                        )
                    }
                }
            }
            item("end") {
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }
    }
}


@Composable
fun MoreCommentsSheet(
    mainController: MainController,
    comment: Comment,
    subComments: List<Comment>,
    commentLikings: Set<Long>,
    loading: Boolean,
    refreshing: Boolean,
    state: LoadableLazyColumnWithoutPullRequestState,

    onCancel: () -> Unit,
    onLikeComment: (Long) -> Unit,

    /**
     * 第一个是主评论，第二个是回复的评论
     */
    onOpenCommentDialog: (Comment, Comment) -> Unit,
    onOpenImages: (Int, List<Image>) -> Unit,
    onReport: (Comment) -> Unit,
    onOpenDeleteCommentDialog: (Comment) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.padding(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "更多回复", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "close config dialog",
                    )
                }
            }
            Spacer(modifier = Modifier.padding(4.dp))

            MoreCommentsSheetContent(
                mainController = mainController,
                comment = comment,
                subComments = subComments,
                commentLikings = commentLikings,
                loading = loading,
                refreshing = refreshing,
                state = state,

                onLikeComment = onLikeComment,
                onOpenImages = onOpenImages,
                onOpenCommentDialog = onOpenCommentDialog,
                onReport = onReport,
                onOpenDeleteCommentDialog = onOpenDeleteCommentDialog,
            )
        }
    }
}