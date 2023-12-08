package cn.bit101.android.ui.gallery.poster

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
     * 点赞状态
     */
    val likings by vm.likingsFlow.collectAsState()
    val posterLiking = likings.contains(ObjectType.PosterObject(id))
    val commentLikings = likings.filterIsInstance(ObjectType.CommentObject::class.java).map { it.comment.id.toLong() }.toSet()

    /**
     * 删除帖子这个动作的状态
     */
    val deletePosterState by vm.deletePosterStateFlow.collectAsState()

    /**
     * 删除评论这个动作的状态
     */
    val deleteCommentState by vm.deleteCommentStateFlow.collectAsState()

    /**
     * 评论是否加载完毕
     */
    val commentLoaded = vm.commentStateExports.pageFlow.collectAsState().value == -1

    /**
     * 子评论是否加载完毕
     */
    val subCommentLoaded = vm.subCommentStateExports.pageFlow.collectAsState().value == -1

    /**
     * 发送评论的状态
     */
    val sendCommentState by vm.sendCommentStateFlow.collectAsState()

    /**
     * 需要评论的类型
     */
    var commentTypeNeedShowCommentBottomSheet by remember { mutableStateOf<CommentType?>(null) }

    /**
     * 选择图片，然后上传图片
     */
    val imagePicker = rememberImagePicker {
        if(commentTypeNeedShowCommentBottomSheet != null) {
            vm.uploadImage(ctx, commentTypeNeedShowCommentBottomSheet!!, it)
        }
    }

    /**
     * 需要显示更多评论的评论
     */
    var commentNeedShowMoreComments by remember { mutableStateOf<Comment?>(null) }

    /**
     * 更多评论的bottom sheet的状态
     */
    val moreCommentsBottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed,
        confirmValueChange = {
            if(it == BottomSheetValue.Expanded) {
                val commentForShowMoreComments = commentNeedShowMoreComments
                if(commentForShowMoreComments != null) vm.subCommentStateExports.refresh(commentForShowMoreComments.id.toLong())
            }
            true
        }
    )

    /**
     * 更多操作的bottom sheet的状态
     */
    var commentNeedShowMoreAction by remember { mutableStateOf<Comment?>(null) }

    /**
     * 更多操作的bottom sheet的状态
     */
    val moreActionBottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed,
    )

    /**
     * 确认删除对帖子的评论中的图片的对话框
     */
    var showCommentImageDialogState by rememberSaveable { mutableIntStateOf(-1) }

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

    // 当删除帖子的状态为Success时，返回主页，显示反馈消息
    LaunchedEffect(deletePosterState) {
        if(deletePosterState is SimpleState.Success) {
            mainController.snackbar("帖子删除成功了！")
            mainController.navController.popBackStack()
        } else if(deletePosterState is SimpleState.Fail) {
            mainController.snackbar("帖子删除失败Orz")
        }
    }

    // 当删除评论的状态为Success时，显示反馈消息
    LaunchedEffect(deleteCommentState) {
        if (deleteCommentState is SimpleState.Success) {
            mainController.snackbar("评论删除成功了！")
        } else if (deleteCommentState is SimpleState.Fail) {
            mainController.snackbar("评论删除失败Orz")
        }
    }

    // 当发送评论的状态为Success时，显示反馈消息，关闭评论bottom sheet
    LaunchedEffect(sendCommentState) {
        if(sendCommentState is SimpleState.Success) {
            mainController.snackbar("评论成功被发出去了！")
            commentTypeNeedShowCommentBottomSheet = null
        } else if(sendCommentState is SimpleState.Fail) {
            mainController.snackbar("评论失败Orz")
        }
    }

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
            onShowMoreComments = {
                commentNeedShowMoreComments = it
                scope.launch { moreCommentsBottomSheetState.expand() }
            },

            onOpenImages = mainController::showImages,
            onOPenCommentToPoster = { commentTypeNeedShowCommentBottomSheet = CommentType.ToPoster(id) },
            onOpenCommentToComment = { c, sc -> commentTypeNeedShowCommentBottomSheet = CommentType.ToComment(c, sc) },
            onOpenEdit = { mainController.navController.navigate("edit/$id") },
            onOpenMoreActionOfCommentBottomSheet = {
                commentNeedShowMoreAction = it
                scope.launch { moreActionBottomSheetState.expand() }
            },
        )

        MoreCommentsBottomSheet(
            mainController = mainController,
            bottomSheetState = moreCommentsBottomSheetState,
            comment = commentNeedShowMoreComments,
            subComments = subComments,
            commentLikings = commentLikings,
            loading = subCommentsLoadMoreState is SimpleState.Loading,
            loaded = subCommentLoaded,
            refreshing = subCommentsRefreshState is SimpleState.Loading,
            state = rememberLoadableLazyColumnWithoutPullRequestState(
                onLoadMore = { vm.subCommentStateExports.loadMore(commentNeedShowMoreComments!!.id.toLong()) }
            ),

            onDismiss = { scope.launch { moreCommentsBottomSheetState.collapse() } },
            onOpenImages = mainController::showImages,
            onLikeComment = { vm.like(ObjectType.CommentObject(it)) },
            onOpenCommentToComment = { c, sc -> commentTypeNeedShowCommentBottomSheet = CommentType.ToComment(c, sc) },
            onOpenDeleteCommentDialog = { vm.deleteCommentById(it.id.toLong()) },
            onReport = { mainController.navigate("report/comment/${it.id}") },
            onOpenMoreActionOfCommentBottomSheet = {
                commentNeedShowMoreAction = it
                scope.launch { moreActionBottomSheetState.expand() }
            },
        )

        MoreActionBottomSheet(
            state = moreActionBottomSheetState,
            onDelete = { vm.deleteCommentById(commentNeedShowMoreAction!!.id.toLong()) },
            onReport = { mainController.navigate("report/comment/${commentNeedShowMoreAction!!.id}") },
            onCopy = { mainController.copyText(cm, commentNeedShowMoreAction?.text) },
            onDismiss = { scope.launch { moreActionBottomSheetState.collapse() } }
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
                onSendComment = { vm.sendComment(commentType, commentEditData) },
                onOpenDeleteImageDialog = { showCommentImageDialogState = it },
                onDismiss = { commentTypeNeedShowCommentBottomSheet = null }
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