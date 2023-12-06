package cn.bit101.android.ui.gallery.poster

import android.content.Intent
import android.provider.MediaStore
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
import cn.bit101.android.ui.component.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.gallery.poster.component.CommentBottomSheet
import cn.bit101.android.ui.gallery.poster.component.MoreActionBottomSheet
import cn.bit101.android.ui.gallery.poster.component.MoreCommentsBottomSheet
import cn.bit101.android.ui.common.rememberImagePicker
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image


@Composable
fun PosterScreen(
    mainController: MainController,
    id: Long,
    onOpenImage: (Image) -> Unit,
    onOpenImages: (Int, List<Image>) -> Unit,
    vm: PosterViewModel = hiltViewModel()
) {
    /**
     * 上下文
     */
    val ctx = LocalContext.current

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
     * 评论底部栏的状态，如果为null，则不打开
     */
    var showCommentBottomSheetState by remember { mutableStateOf<CommentType?>(null) }

    val imagePicker = rememberImagePicker {
        if(showCommentBottomSheetState != null) {
            vm.uploadImage(ctx, showCommentBottomSheetState!!, it)
        }
    }

    /**
     * 上传图片时发送的intent
     */
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
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
    var deletePosterDialogState by rememberSaveable { mutableIntStateOf(-1) }

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
            deletePosterDialogState = -1
            vm.setDeletePosterState(null)
        } else if(deletePosterState is SimpleState.Fail) {
            mainController.snackbar("帖子删除失败Orz")
            vm.setDeletePosterState(null)
        }
    }

    /**
     * 确认删除Comment的对话框
     */
    var deleteCommentDialogState by rememberSaveable { mutableIntStateOf(-1) }

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
            deleteCommentDialogState = -1
            vm.setShowMoreState(false, null)
            vm.commentStateExports.refresh(id)
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
     * 确认删除对评论的评论中的图片的对话框
     */
    var deleteImageOfCommentDialogState by rememberSaveable { mutableStateOf<Pair<Pair<Comment, Comment>, Int>?>(null) }

    /**
     * 确认删除对帖子的评论中的图片的对话框
     */
    var deleteImageOfPosterDialogState by rememberSaveable { mutableIntStateOf(-1) }

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
        } else if(sendCommentState is SimpleState.Fail && needShowSnackbarForCommentToComment) {
            mainController.snackbar("评论失败Orz")
            needShowSnackbarForCommentToComment = false
        }
    }

    var showMoreActionOfCommentState by remember { mutableStateOf<Comment?>(null) }


    /**
     * 评论是否加载完毕
     */
    val commentLoaded = vm.commentStateExports.pageFlow.collectAsState().value == -1

    /**
     * 子评论是否加载完毕
     */
    val subCommentLoaded = vm.subCommentStateExports.pageFlow.collectAsState().value == -1

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
            showCommentBottomSheetState = CommentType.ToComment(comment, subComment)
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

            onOpenImages = onOpenImages,
            onOpenCommentToComment = onOpenCommentToComment,
            onOpenEdit = { mainController.navController.navigate("edit/$id") },
            onOpenMoreActionOfCommentBottomSheet = { showMoreActionOfCommentState = it },
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
                onOpenImages = onOpenImages,
                onLikeComment = { vm.like(ObjectType.CommentObject(it)) },
                onOpenCommentToComment = onOpenCommentToComment,
                onOpenDeleteCommentDialog = { deleteCommentDialogState = it.id },
                onReport = { mainController.navigate("report/comment/${it.id}") },
                onOpenMoreActionOfCommentBottomSheet = { showMoreActionOfCommentState = it },
            )
        }

        if(showMoreActionOfCommentState != null) {
            val commentForActions = showMoreActionOfCommentState!!

            MoreActionBottomSheet(
                onDelete = { vm.deleteCommentById(commentForActions.id.toLong()) },
                onReport = { mainController.navigate("report/comment/${commentForActions.id}") },
                onCopy = { mainController.copyText(cm, commentForActions.text) },
                onDismiss = { showMoreActionOfCommentState = null }
            )
        }

        if(showCommentBottomSheetState != null) {
            val commentType = showCommentBottomSheetState!!

            CommentBottomSheet(
                commentType = commentType,
                commentEditData = commentEditDataMap[commentType] ?: CommentEditData.empty(),
                sending = sendCommentState is SimpleState.Loading,
                onEditComment = vm::setCommentEditData,
                onOpenImage = onOpenImage,
                onUploadImage = { imagePicker.pickImage() },
                onSendComment = vm::sendComment,
                onOpenDeleteImageDialog = {},
                onDismiss = { showCommentBottomSheetState = null }
            )
        }

        if(deletePosterDialogState != -1) {
            DeletePosterDialog(
                onConfirm = { vm.deletePosterById(id) },
                onDismiss = { deletePosterDialogState = -1 }
            )
        }

        if(deleteCommentDialogState != -1) {
            DeleteCommentDialog(
                onConfirm = { vm.deleteCommentById(deleteCommentDialogState.toLong()) },
                onDismiss = { deleteCommentDialogState = -1 }
            )
        }
    } else {

    }
}