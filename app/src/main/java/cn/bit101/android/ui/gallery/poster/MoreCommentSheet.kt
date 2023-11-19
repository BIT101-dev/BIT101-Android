package cn.bit101.android.ui.gallery.poster

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.gallery.component.CommentCard
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import java.net.URLEncoder


@Composable
fun MoreCommentsSheetContent(
    mainController: MainController,
    comment: Comment,
    subComments: List<Comment>,
    commentLikes: Set<Long>,
    loading: Boolean,
    refreshing: Boolean,
    state: LoadableLazyColumnWithoutPullRequestState,

    onLikeComment: (Long) -> Unit,
    /**
     * 第一个是主评论，第二个是回复的评论
     */
    onOpenImages: (Int, List<Image>) -> Unit,
    onOpenCommentDialog: (Comment, Comment) -> Unit,
    onReport: (Comment) -> Unit,
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
                    commentLikes = commentLikes,
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
                            onOpenImage = { onOpenImages(it, comment.images) },
                            showSubComments = false,
                            commentLikes = commentLikes,
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
    commentLikes: Set<Long>,
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
                commentLikes = commentLikes,
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