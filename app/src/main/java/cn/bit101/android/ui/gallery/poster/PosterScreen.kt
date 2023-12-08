package cn.bit101.android.ui.gallery.poster

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.gallery.DeleteCommentDialog
import cn.bit101.android.ui.component.gallery.DeletePosterDialog
import cn.bit101.android.ui.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.gallery.poster.component.CommentBottomSheet
import cn.bit101.android.ui.gallery.poster.component.MoreActionBottomSheet
import cn.bit101.android.ui.gallery.poster.component.MoreCommentsBottomSheet
import cn.bit101.android.ui.common.rememberImagePicker
import cn.bit101.android.ui.component.bottomsheet.BottomSheetValue
import cn.bit101.android.ui.component.bottomsheet.rememberBottomSheetState
import cn.bit101.android.ui.component.gallery.DeleteImageDialog
import cn.bit101.api.model.common.Comment
import kotlinx.coroutines.launch


@Composable
fun PosterScreen(
    mainController: MainController,
    id: Long,
    vm: PosterViewModel = hiltViewModel()
) {
    /**
     * 上下文
     */
    val ctx = LocalContext.current

    /**
     * 协程作用域
     */
    val scope = rememberCoroutineScope()

    /**
     * 剪贴板
     */
    val cm = LocalClipboardManager.current

    /**
     * 获取帖子这个动作的状态
     */
    val getPosterState by vm.getPosterStateFlow.collectAsState()

    /**
     * 加载更多评论这个动作的状态
     */
    val loadMoreState by vm.commentStateExports.loadMoreStateFlow.collectAsState() // 加载更多评论

    /**
     * 刷新评论这个动作的状态
     */
    val refreshState by vm.commentStateExports.refreshStateFlow.collectAsState() // 刷新

    /**
     * 对帖子的评论的编辑数据
     */
    val commentEditDataMap by vm.commentEditDataMapFlow.collectAsState()

    /**
     * 子评论的刷新状态
     */
    val subCommentsRefreshState by vm.subCommentStateExports.refreshStateFlow.collectAsState()

    /**
     * 子评论的加载更多状态
     */
    val subCommentsLoadMoreState by vm.subCommentStateExports.loadMoreStateFlow.collectAsState()

    /**
     * 子评论
     */
    val subComments by vm.subCommentStateExports.dataFlow.collectAsState()

    /**
     * 所有的评论
     */
    val comments by vm.commentStateExports.dataFlow.collectAsState()

    /**
     * 通过bottom sheet展开的评论
     */
    val showComment by vm.showMoreStateFlow.collectAsState()

    /**
     * 点赞状态
     */
    val likings by vm.likingsFlow.collectAsState()

    /**
     * 需要评论的类型
     */
    var commentTypeNeedShowCommentBottomSheet by remember { mutableStateOf<CommentType?>(null) }


    val imagePicker = rememberImagePicker {
        if(commentTypeNeedShowCommentBottomSheet != null) {
            vm.uploadImage(ctx, commentTypeNeedShowCommentBottomSheet!!, it)
        }
    }


    // 打开时获取帖子内容
    LaunchedEffect(getPosterState) {
        if(getPosterState == null) {
            vm.getPosterById(id)
        }
    }

    // 打开时刷新评论
    LaunchedEffect(refreshState) {
        if(refreshState == null) {
            vm.commentStateExports.refresh(id)
        }
    }

    /**
     * 确认删除poster的对话框
     */
    var showDeletePosterDialogState by rememberSaveable { mutableIntStateOf(-1) }

    /**
     * 删除帖子这个动作的状态
     */
    val deletePosterState by vm.deletePosterStateLiveData.observeAsState()

    /**
     * 当删除帖子的状态为Success时，返回主页，关闭对话框，显示反馈消息
     */
    LaunchedEffect(deletePosterState) {
        if(deletePosterState is SimpleState.Success) {
            mainController.snackbar("帖子删除成功了！")
            mainController.navController.popBackStack()
            showDeletePosterDialogState = -1
            vm.setDeletePosterState(null)
        } else if(deletePosterState is SimpleState.Fail) {
            mainController.snackbar("帖子删除失败Orz")
            vm.setDeletePosterState(null)
        }
    }

    /**
     * 确认删除Comment的对话框
     */
    var showDeleteCommentDialogState by rememberSaveable { mutableIntStateOf(-1) }

    /**
     * 删除评论这个动作的状态
     */
    val deleteCommentState by vm.deleteCommentStateLiveData.observeAsState()

    /**
     * 当删除评论的状态为Success时，关闭对话框，同时关闭bottom sheet，刷新评论，显示反馈消息
     */
    LaunchedEffect(deleteCommentState) {
        if (deleteCommentState is SimpleState.Success) {
            mainController.snackbar("评论删除成功了！")
            showDeleteCommentDialogState = -1
            vm.setShowMoreState(false, null)
            vm.setDeleteCommentState(null)
        } else if (deleteCommentState is SimpleState.Fail) {
            mainController.snackbar("评论删除失败Orz")
            vm.setDeleteCommentState(null)
        }
    }

    /**
     * 如果显示更多评论的状态改变了，并且要显示bottom sheet，且要显示的评论不为null，那么就获取这个评论的子评论
     */
    DisposableEffect(showComment) {
        if(showComment.second != null && showComment.first) {
            vm.subCommentStateExports.refresh(showComment.second!!.toLong())
        }
        onDispose {  }
    }

    /**
     * 确认删除对帖子的评论中的图片的对话框
     */
    var showCommentImageDialogState by rememberSaveable { mutableIntStateOf(-1) }

    /**
     * 发送评论的状态
     */
    val sendCommentState by vm.sendCommentStateFlow.collectAsState()
    var needShowSnackbarForCommentToComment by rememberSaveable { mutableStateOf(false) }

    /**
     * 当发送评论的状态为Success时，显示Snackbar
     */
    LaunchedEffect(sendCommentState, needShowSnackbarForCommentToComment) {
        if(sendCommentState is SimpleState.Success && needShowSnackbarForCommentToComment) {
            mainController.snackbar("评论成功被发出去了！")
            needShowSnackbarForCommentToComment = false
            commentTypeNeedShowCommentBottomSheet = null
        } else if(sendCommentState is SimpleState.Fail && needShowSnackbarForCommentToComment) {
            mainController.snackbar("评论失败Orz")
            needShowSnackbarForCommentToComment = false
        }
    }


    /**
     * 评论是否加载完毕
     */
    val commentLoaded = vm.commentStateExports.pageFlow.collectAsState().value == -1

    /**
     * 子评论是否加载完毕
     */
    val subCommentLoaded = vm.subCommentStateExports.pageFlow.collectAsState().value == -1


    var commentNeedShowMoreAction by remember { mutableStateOf<Comment?>(null) }
    val moreActionBottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed,
    )

    if(getPosterState is SimpleDataState.Loading || refreshState is SimpleState.Loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else if(getPosterState is SimpleDataState.Success && refreshState is SimpleState.Success) {

        val posterLiking = likings.contains(ObjectType.PosterObject(id))
        val commentLikings = likings.filterIsInstance(ObjectType.CommentObject::class.java).map { it.comment.id.toLong() }.toSet()

        val onOpenCommentToComment: (Comment, Comment) -> Unit = { comment, subComment ->
            commentTypeNeedShowCommentBottomSheet = CommentType.ToComment(comment, subComment)
        }

        PosterContent(
            mainController = mainController,

            data = (getPosterState as SimpleDataState.Success).data,
            comments = comments,
            posterLiking = posterLiking,
            commentLikings = commentLikings,
            loading = loadMoreState is SimpleState.Loading,
            loaded = commentLoaded,
            state = rememberLoadableLazyColumnWithoutPullRequestState(
                onLoadMore = { vm.commentStateExports.loadMore(id) }
            ),

            onLikePoster = { vm.like(ObjectType.PosterObject(id)) },
            onLikeComment = { vm.like(ObjectType.CommentObject(it)) },
            onShowMoreComments = { vm.setShowMoreState(true, it.id.toLong()) },

            onOpenImages = mainController::showImages,
            onOPenCommentToPoster = { commentTypeNeedShowCommentBottomSheet = CommentType.ToPoster(id) },
            onOpenCommentToComment = onOpenCommentToComment,
            onOpenEdit = { mainController.navController.navigate("edit/$id") },
            onOpenMoreActionOfCommentBottomSheet = {
                commentNeedShowMoreAction = it
                scope.launch { moreActionBottomSheetState.expand() }
            },
        )

        if(showComment.first) {
            val commentId = showComment.second!!
            val comment = vm.findCommentById(commentId)!!
            MoreCommentsBottomSheet(
                mainController = mainController,
                comment = comment,
                subComments = subComments,
                commentLikings = commentLikings,
                loading = subCommentsLoadMoreState is SimpleState.Loading,
                loaded = subCommentLoaded,
                refreshing = subCommentsRefreshState is SimpleState.Loading,
                state = rememberLoadableLazyColumnWithoutPullRequestState(
                    onLoadMore = { vm.subCommentStateExports.loadMore(commentId) }
                ),

                onCancel = { vm.setShowMoreState(false) },
                onOpenImages = mainController::showImages,
                onLikeComment = { vm.like(ObjectType.CommentObject(it)) },
                onOpenCommentToComment = onOpenCommentToComment,
                onOpenDeleteCommentDialog = { showDeleteCommentDialogState = it.id },
                onReport = { mainController.navigate("report/comment/${it.id}") },
                onOpenMoreActionOfCommentBottomSheet = {
                    commentNeedShowMoreAction = it
                    scope.launch { moreActionBottomSheetState.expand() }
                },
            )
        }

        val commentForMoreAction = commentNeedShowMoreAction

        MoreActionBottomSheet(
            state = moreActionBottomSheetState,
            onDelete = {
                if(commentForMoreAction != null) vm.deleteCommentById(commentForMoreAction.id.toLong())
            },
            onReport = {
                if(commentForMoreAction != null) mainController.navigate("report/comment/${commentForMoreAction.id}")
            },
            onCopy = {
                if(commentForMoreAction != null) mainController.copyText(cm, commentForMoreAction.text)
            },
            onDismiss = {
                commentNeedShowMoreAction = null
                scope.launch { moreActionBottomSheetState.collapse() }
            }
        )

        if(commentTypeNeedShowCommentBottomSheet != null) {
            val commentType = commentTypeNeedShowCommentBottomSheet!!
            val commentEditData = commentEditDataMap[commentType] ?: CommentEditData.empty()

            CommentBottomSheet(
                commentType = commentType,
                commentEditData = commentEditData,
                sending = sendCommentState is SimpleState.Loading,
                onEditComment = { vm.setCommentEditData(commentType, it) },
                onOpenImage = mainController::showImage,
                onUploadImage = { imagePicker.pickImage() },
                onSendComment = {
                    needShowSnackbarForCommentToComment = true
                    vm.sendComment(commentType, commentEditData)
                },
                onOpenDeleteImageDialog = { showCommentImageDialogState = it },
                onDismiss = { commentTypeNeedShowCommentBottomSheet = null }
            )
        }

        if(showDeletePosterDialogState != -1) {
            DeletePosterDialog(
                onConfirm = { vm.deletePosterById(id) },
                onDismiss = { showDeletePosterDialogState = -1 }
            )
        }

        if(showDeleteCommentDialogState != -1) {
            DeleteCommentDialog(
                onConfirm = { vm.deleteCommentById(showDeleteCommentDialogState.toLong()) },
                onDismiss = { showDeleteCommentDialogState = -1 }
            )
        }

        if(showCommentImageDialogState != -1) {
            val commentType = commentTypeNeedShowCommentBottomSheet!!
            val index = showCommentImageDialogState
            DeleteImageDialog(
                onConfirm = { vm.deleteImageOfComment(commentType, index) },
                onDismiss = { showCommentImageDialogState = -1 }
            )
        }

    } else {

    }
}