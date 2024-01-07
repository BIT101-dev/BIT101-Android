package cn.bit101.android.ui.gallery.poster

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.loadable.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.gallery.poster.component.CommentBottomSheet
import cn.bit101.android.ui.gallery.poster.component.MoreActionOfCommentBottomSheet
import cn.bit101.android.ui.common.rememberImagePicker
import cn.bit101.android.ui.component.bottomsheet.BottomSheetValue
import cn.bit101.android.ui.component.bottomsheet.rememberBottomSheetState
import cn.bit101.android.ui.component.gallery.DeleteImageDialog
import cn.bit101.android.ui.gallery.poster.component.MoreActionOfPosterBottomSheet
import cn.bit101.android.ui.gallery.poster.component.MoreCommentsPage
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
    var commentIdForShowMoreComments by rememberSaveable { mutableStateOf<Long?>(null) }

    /**
     * 更多评论的bottom sheet的状态
     */
    var showMoreCommentsPage by rememberSaveable { mutableStateOf(false) }

    /**
     * 更多操作的bottom sheet的状态
     */
    var commentNeedShowMoreAction by rememberSaveable { mutableStateOf<Comment?>(null) }

    /**
     * 评论的更多操作的bottom sheet的状态
     */
    val moreActionOfCommentBottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed,
    )

    /**
     * 帖子更多操作的bottom sheet的状态
     */
    val moreActionOfPosterBottomSheetState = rememberBottomSheetState(
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

    // 清空状态
    DisposableEffect(Unit) {
        onDispose { vm.clearStates() }
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

        val mainListState = rememberLoadableLazyColumnWithoutPullRequestState(
            onLoadMore = { vm.commentStateExports.loadMore(id) }
        )

        AnimatedContent(
            targetState = showMoreCommentsPage,
            transitionSpec = {
                // 推入的效果
                if(!targetState) {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(200)
                        ),
                        initialContentExit = slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(200)
                        ),
                    )
                } else {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(200)
                        ),
                        initialContentExit = slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(200)
                        ),
                    )
                }
            },
            label = "poster screen content",
        ) { showMoreComment ->
            if(showMoreComment) {
                MoreCommentsPage(
                    mainController = mainController,
                    comment = vm.findCommentById(commentIdForShowMoreComments!!),
                    subComments = subComments,
                    commentLikings = commentLikings,
                    loading = subCommentsLoadMoreState is SimpleState.Loading,
                    loaded = subCommentLoaded,
                    refreshing = subCommentsRefreshState is SimpleState.Loading,
                    state = rememberLoadableLazyColumnWithoutPullRequestState(
                        onLoadMore = { vm.subCommentStateExports.loadMore(commentIdForShowMoreComments!!) }
                    ),

                    onDismiss = { showMoreCommentsPage = false },
                    onOpenImages = mainController::showImages,
                    onLikeComment = { vm.like(ObjectType.CommentObject(it)) },
                    onOpenCommentToComment = { c, sc -> commentTypeNeedShowCommentBottomSheet = CommentType.ToComment(c, sc) },
                    onOpenMoreActionOfCommentBottomSheet = {
                        commentNeedShowMoreAction = it
                        scope.launch { moreActionOfCommentBottomSheetState.expand() }
                    },
                )
            } else {
                PosterContent(
                    mainController = mainController,

                    data = (getPosterState as SimpleDataState.Success).data,
                    comments = comments,
                    posterLiking = posterLiking,
                    commentLikings = commentLikings,
                    loading = loadMoreState is SimpleState.Loading,
                    loaded = commentLoaded,
                    state = mainListState,

                    onLikePoster = { vm.like(ObjectType.PosterObject(id)) },
                    onLikeComment = { vm.like(ObjectType.CommentObject(it)) },
                    onShowMoreComments = {
                        commentIdForShowMoreComments = it.id.toLong()
                        vm.subCommentStateExports.refresh(it.id.toLong())
                        showMoreCommentsPage = true
                    },

                    onOpenImages = mainController::showImages,
                    onOPenCommentToPoster = { commentTypeNeedShowCommentBottomSheet = CommentType.ToPoster(id) },
                    onOpenCommentToComment = { c, sc -> commentTypeNeedShowCommentBottomSheet = CommentType.ToComment(c, sc) },
                    onOpenMoreActionOfCommentBottomSheet = {
                        commentNeedShowMoreAction = it
                        scope.launch { moreActionOfCommentBottomSheetState.expand() }
                    },
                    onOpenMoreActionOfPosterBottomSheet = {
                        scope.launch { moreActionOfPosterBottomSheetState.expand() }
                    },
                )
            }
        }

        MoreActionOfCommentBottomSheet(
            state = moreActionOfCommentBottomSheetState,
            onDelete = { vm.deleteCommentById(commentNeedShowMoreAction!!.id.toLong()) },
            onReport = { mainController.navigate("report/comment/${commentNeedShowMoreAction!!.id}") },
            onCopy = { mainController.copyText(cm, commentNeedShowMoreAction?.text) },
            onDismiss = { scope.launch { moreActionOfCommentBottomSheetState.collapse() } }
        )

        MoreActionOfPosterBottomSheet(
            state = moreActionOfPosterBottomSheetState,
            onEdit = { mainController.navigate("edit/$id") },
            onDelete = { vm.deletePosterById(id) },
            onReport = { mainController.navigate("report/poster/$id") },
            onOpenInBrowser = { mainController.openPoster(id, ctx) },

            onDismiss = { scope.launch { moreActionOfPosterBottomSheetState.collapse() } }
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
    } else if(getPosterState is SimpleDataState.Fail || refreshState is SimpleState.Fail) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = "错误"
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "加载失败了Orz",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
        }
    }
}