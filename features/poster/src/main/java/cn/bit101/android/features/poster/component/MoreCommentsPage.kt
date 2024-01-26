package cn.bit101.android.features.poster.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.CustomDivider
import cn.bit101.android.features.common.component.gallery.CommentCard
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image


@Composable
internal fun MoreCommentsContent(
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
     * 是否已经加载完所有评论
     */
    loaded: Boolean,

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
    onLikeComment: (Comment) -> Unit,

    /**
     * 打开图片组
     */
    onOpenImages: (Int, List<Image>) -> Unit,

    /**
     * 打开*对评论的评论*的编辑框，第一个是主评论，第二个是回复的评论
     */
    onOpenCommentDialog: (Comment, Comment) -> Unit,

    /**
     * 打开评论的更多操作
     */
    onOpenMoreActionOfCommentBottomSheet: (Comment) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        LoadableLazyColumnWithoutPullRequest(
            loading = loading,
            state = state,
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            item(comment.id) {
                CommentCard(
                    mainController = mainController,
                    comment = comment,
                    showDivider = false,
                    onOpenImage = onOpenImages,
                    showSubComments = false,
                    commentLikings = commentLikings,
                    onLikeComment = onLikeComment,
                    onOpenCommentToComment = onOpenCommentDialog,
                    onMoreAction = onOpenMoreActionOfCommentBottomSheet
                )
                CustomDivider()
            }

            item("reply header") {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = "回复 ${comment.commentNum}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.padding(4.dp))
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
                            onOpenImage = onOpenImages,
                            showSubComments = false,
                            commentLikings = commentLikings,
                            onLikeComment = onLikeComment,
                            onOpenCommentToComment = { subComment, _ ->
                                onOpenCommentDialog(comment, subComment)
                            },
                            onMoreAction = onOpenMoreActionOfCommentBottomSheet
                        )
                    }
                }
            }

            if(loaded) {
                item("footer") {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "没有更多评论了",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else if(!loading) {
                item("footer2") {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "上滑查看更多哦",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            item("end") {
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreCommentsPage(
    mainController: MainController,
    comment: Comment?,
    subComments: List<Comment>,
    commentLikings: Set<Long>,
    loading: Boolean,
    loaded: Boolean,
    refreshing: Boolean,
    state: LoadableLazyColumnWithoutPullRequestState,

    onDismiss: () -> Unit,
    onLikeComment: (Comment) -> Unit,

    /**
     * 第一个是主评论，第二个是回复的评论
     */
    onOpenCommentToComment: (Comment, Comment) -> Unit,
    onOpenImages: (Int, List<Image>) -> Unit,
    onOpenMoreActionOfCommentBottomSheet: (Comment) -> Unit,
) {
    if(comment == null) return

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "更多回复",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "关闭",
                        )
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
        ) {
            MoreCommentsContent(
                mainController = mainController,
                comment = comment,
                subComments = subComments,
                commentLikings = commentLikings,
                loading = loading,
                loaded = loaded,
                refreshing = refreshing,
                state = state,

                onLikeComment = onLikeComment,
                onOpenImages = onOpenImages,
                onOpenCommentDialog = onOpenCommentToComment,
                onOpenMoreActionOfCommentBottomSheet = onOpenMoreActionOfCommentBottomSheet,
            )
        }
    }
}