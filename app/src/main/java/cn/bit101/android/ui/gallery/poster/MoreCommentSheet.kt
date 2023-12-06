package cn.bit101.android.ui.gallery.poster

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.gallery.CommentCard
import cn.bit101.android.utils.ColorUtils
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import cn.bit101.android.ui.component.bottomsheet.BottomSheetValue
import cn.bit101.android.ui.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.ui.component.bottomsheet.BottomSheet
import cn.bit101.android.ui.component.bottomsheet.BottomSheetDefaults
import cn.bit101.android.ui.component.bottomsheet.rememberBottomSheetState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
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
                    onClick = {
                        // 第一个是主评论，第二个是回复的评论
                        onOpenCommentDialog(comment, comment)
                    },
                    onMoreAction = onOpenMoreActionOfCommentBottomSheet
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    val cornerRadius = 8.dp
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {}
                    Spacer(modifier = Modifier.padding(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {}
                }
            }

            item("reply header") {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    text = "回复 ${comment.commentNum}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
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
                            onOpenImage = onOpenImages,
                            showSubComments = false,
                            commentLikings = commentLikings,
                            onLikeComment = onLikeComment,
                            onClick = { onOpenCommentDialog(comment, sub) },
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


@Composable
fun MoreCommentsSheet(
    mainController: MainController,
    comment: Comment,
    subComments: List<Comment>,
    commentLikings: Set<Long>,
    loading: Boolean,
    loaded: Boolean,
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
    onOpenMoreActionOfCommentBottomSheet: (Comment) -> Unit,
) {
    BackHandler {
        onCancel()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
    ) {}

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .systemBarsPadding()
            .padding(top = 32.dp),
        shape = BottomSheetDefaults.shape,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "评论回复",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(20.dp),
                    onClick = onCancel
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "关闭",
                    )
                }
            }
            MoreCommentsSheetContent(
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
                onOpenCommentDialog = onOpenCommentDialog,
                onReport = onReport,
                onOpenDeleteCommentDialog = onOpenDeleteCommentDialog,
                onOpenMoreActionOfCommentBottomSheet = onOpenMoreActionOfCommentBottomSheet,
            )
        }
    }

//    val scope = rememberCoroutineScope()
//    val bottomSheet = rememberBottomSheetState(
//        initialValue = BottomSheetValue.Expanded,
//        confirmValueChange = {
//            if(it == BottomSheetValue.Collapsed) {
//                onCancel()
//                false
//            } else true
//        },
//    )
//
//    BottomSheet(
//        state = bottomSheet,
//        skipPeeked = true,
//        allowNestedScroll = false,
//        dragHandle = {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 16.dp),
//            ) {
//                Text(
//                    modifier = Modifier.align(Alignment.Center),
//                    text = "评论回复",
//                    style = MaterialTheme.typography.titleMedium.copy(
//                        fontWeight = FontWeight.Bold
//                    )
//                )
//                IconButton(
//                    modifier = Modifier
//                        .align(Alignment.CenterStart)
//                        .size(20.dp),
//                    onClick = { scope.launch { bottomSheet.collapse() } }
//                ) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
//                        contentDescription = "关闭",
//                    )
//                }
//            }
//        },
//        behaviors = DialogSheetBehaviors(
//            extendsIntoStatusBar = false,
//            extendsIntoNavigationBar = false,
//            lightNavigationBar = ColorUtils.isLightColor(MaterialTheme.colorScheme.background),
//            navigationBarColor = MaterialTheme.colorScheme.surfaceContainer,
//        ),
//        backgroundColor = MaterialTheme.colorScheme.surface,
//    ) {
//        Column(modifier = Modifier.fillMaxWidth()) {
//            MoreCommentsSheetContent(
//                mainController = mainController,
//                comment = comment,
//                subComments = subComments,
//                commentLikings = commentLikings,
//                loading = loading,
//                refreshing = refreshing,
//                state = state,
//
//                onLikeComment = onLikeComment,
//                onOpenImages = onOpenImages,
//                onOpenCommentDialog = onOpenCommentDialog,
//                onReport = onReport,
//                onOpenDeleteCommentDialog = onOpenDeleteCommentDialog,
//                onOpenMoreActionOfCommentBottomSheet = onOpenMoreActionOfCommentBottomSheet,
//            )
//        }
//    }

}