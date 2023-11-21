package cn.bit101.android.ui.gallery.poster

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.PreviewImage
import cn.bit101.android.ui.component.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.gallery.common.LoadMoreState
import cn.bit101.android.ui.gallery.common.RefreshState
import cn.bit101.android.ui.gallery.common.SimpleState
import cn.bit101.android.ui.gallery.common.UploadImageData
import cn.bit101.android.ui.component.gallery.CommentCard
import cn.bit101.android.ui.component.gallery.CommentEditContent
import cn.bit101.android.ui.component.gallery.AnnotatedText
import cn.bit101.android.ui.component.gallery.DeleteCommentDialog
import cn.bit101.android.ui.component.gallery.DeleteImageDialog
import cn.bit101.android.ui.component.gallery.DeletePosterDialog
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPosterDataModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PosterContent(
    mainController: MainController,

    /**
     * 是否正在加载评论
     */
    loading: Boolean,

    /**
     * 所有的评论
     */
    comments: List<Comment>,

    /**
     * 评论区的加载状态，这里只有加载更多和，没有下拉刷新
     */
    state: LoadableLazyColumnWithoutPullRequestState,

    /**
     * 正在进行点赞操作的评论的ID，存储在一个Set中，如果评论的id在Set中，说明正在进行点赞操作，正在点赞的需要转圈圈
     */
    commentLikings: Set<Long>,

    /**
     * 是否正在对帖子进行点赞
     */
    posterLiking: Boolean,

    /**
     * 帖子的数据
     */
    data: GetPosterDataModel.Response,

    /**
     * *对帖子的评论*编辑的数据
     */
    commentEditData: CommentEditData,

    /**
     * 对当前的帖子进行点赞
     */
    onLikePoster: () -> Unit,

    /**
     * 对帖子进行评论，需要传入*对帖子的评论*的编辑数据
     */
    onEditComment: (CommentEditData) -> Unit,

    /**
     * 对评论点赞，需要传入评论的ID
     */
    onLikeComment: (Long) -> Unit,

    /**
     * 显示更多评论，需要传入*需要显示子评论的评论*的数据
     */
    onShowMoreComments: (Comment) -> Unit,

    /**
     * 向帖子发送评论，需要传入*对帖子的评论*的编辑数据
     */
    onSendCommentToPoster: (CommentEditData) -> Unit,

    /**
     * 上传图片
     */
    onUploadImage: () -> Unit,

    /**
     * 打开对帖子的举报对话框
     */
    onOpenReportPoster: () -> Unit,

    /**
     * 打开对评论的举报对话框
     */
    onOpenReportComment: (Comment) -> Unit,

    /**
     * 打开对帖子的删除对话框
     */
    onOpenDeletePosterDialog: () -> Unit,

    /**
     * 打开对评论的删除对话框
     */
    onOpenDeleteCommentDialog: (Comment) -> Unit,

    /**
     * 打开图片
     */
    onOpenImage: (Image) -> Unit,

    /**
     * 打开图片组，第一个参数是默认显示的图片序号，第二个参数是url列表
     */
    onOpenImages: (Int, List<Image>) -> Unit,

    /**
     * 打开编辑帖子的对话框
     */
    onOpenEdit: () -> Unit,

    /**
     * 打开*对评论的评论*的编辑对话框，第一个参数是主评论，第二个参数是子评论
     */
    onOpenCommentDialog: (Comment, Comment) -> Unit,

    /**
     * 打开删除*对帖子的评论*中的图片的对话框
     */
    onOpenDeleteImageOfPosterDialog: (Int) -> Unit,
) {
    val cm = LocalClipboardManager.current

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // poster内容卡片
        LoadableLazyColumnWithoutPullRequest(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            loading = loading,
            state = state,
        ) {
            // 标题
            item(data.title) {
                SelectionContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Spacer(modifier = Modifier.padding(10.dp))
            }

            // 用户信息
            item(2) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row {
                        Avatar(
                            user = data.user,
                            low = true,
                            onClick = { mainController.navController.navigate("user/${data.user.id}") }
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = data.user.nickname,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = data.user.identity.text,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    if(data.claim.id != 0) {
                        SuggestionChip(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            shape = CircleShape,
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = "claim",
                                )
                            },
                            onClick = {},
                            label = {
                                Text(
                                    text = data.claim.text,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                    )
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(4.dp))
            }

            item(3) {
                if(data.own && !data.public) {
                    Text(
                        text = "仅自己可见",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.padding(1.dp))
                }
            }
            item(11) {
                ClickableText(
                    text = buildAnnotatedString { append("POSTER ID：" + data.id) },
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    onClick = {
                        mainController.copyText(cm, buildAnnotatedString { append(data.id.toString()) })
                    }
                )
                Spacer(modifier = Modifier.padding(1.dp))
            }

            item(12) {
                Text(
                    text = "首次创建时间：" + DateTimeUtils.format(DateTimeUtils.formatTime(data.createTime)),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.padding(1.dp))
            }

            item(13) {
                Text(
                    text = "最后修改时间：" + DateTimeUtils.format(DateTimeUtils.formatTime(data.updateTime)),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // 帖子信息
            item(14) {
                Spacer(modifier = Modifier.padding(10.dp))
            }

            // 内容
            item(4) {
                // 正文
                if(data.text.isNotEmpty()) {
                    SelectionContainer {
                        AnnotatedText(
                            mainController = mainController,
                            text = data.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // 图片
            item(5) {
                if(data.images.isNotEmpty()) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    LazyRow {
                        itemsIndexed(data.images, { i, _ -> i }) { index, image ->
                            PreviewImage(
                                image = image,
                                onClick = { onOpenImages(index, data.images) },
                            )
                        }
                    }
                }
            }

            // 标签
            item(6) {
                if(data.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    FlowRow {
                        data.tags.forEach { tag ->
                            SuggestionChip(
                                modifier = Modifier.padding(end = 8.dp),
                                shape = CircleShape,
                                onClick = { mainController.copyText(cm, buildAnnotatedString { append(tag) }) },
                                label = { Text(text = tag) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
            }

            // 举报、分享、赞同（还有编辑、删除按钮）
            item(7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // 举报
                    Button(
                        onClick = onOpenReportPoster,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                        )
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = "举报",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "举报",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                    if(data.own) {
                        // 删除
                        IconButton(onClick = onOpenDeletePosterDialog) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "删除",
                            )
                        }
                        // 编辑
                        IconButton(onClick = onOpenEdit) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "编辑",
                            )
                        }
                    }
                    // 点赞
                    Box {
                        IconButton(
                            onClick = onLikePoster,
                            colors = if (data.like) IconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.tertiary,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = MaterialTheme.colorScheme.tertiary,
                            ) else IconButtonDefaults.iconButtonColors(),
                            enabled = !posterLiking
                        ) {
                            if (posterLiking) {
                                CircularProgressIndicator()
                            } else {
                                Icon(
                                    imageVector = if (data.like) Icons.Rounded.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "点赞",
                                )
                            }
                        }

                        if(data.likeNum > 0) Badge(modifier = Modifier.align(Alignment.TopEnd)) {
                            Text(text = data.likeNum.toString())
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
            }

            // 评论编辑
            item(8) {
                CommentEditContent(
                    commentEditData = commentEditData,
                    sending = false,
                    onEditComment = onEditComment,
                    onSendComment = onSendCommentToPoster,
                    onUploadImage = onUploadImage,
                    onOpenImage = onOpenImage,
                    onOpenDeleteImageDialog = onOpenDeleteImageOfPosterDialog,
                )
                Spacer(modifier = Modifier.padding(8.dp))
            }

            // 评论展示

            items(comments, { it.id + 100 }) { comment ->
                CommentCard(
                    mainController = mainController,
                    comment = comment,
                    onOpenImage = { onOpenImages(it, comment.images) },
                    onShowMoreComments = { onShowMoreComments(comment) },
                    commentLikings = commentLikings,
                    onLikeComment = { onLikeComment(comment.id.toLong()) },
                    onClick = { onOpenCommentDialog(comment, comment) },
                    onReport = { onOpenReportComment(comment) },
                    onOpenDeleteCommentDialog = { onOpenDeleteCommentDialog(comment) },
                )
            }

            item(10) {
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }

        val fabSize = 42.dp
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp, 20.dp)
        ) {
            val show by remember { derivedStateOf { state.lazyListState.firstVisibleItemIndex > 7 } }
            AnimatedVisibility(
                visible = show,
                enter = fadeIn(),
                exit = fadeOut(),

                ) {
                SmallFloatingActionButton(
                    modifier = Modifier.size(fabSize),
                    onClick = {
                        scope.launch {
                            state.lazyListState.animateScrollToItem(6, 0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Comment,
                        contentDescription = "回到评论"
                    )
                }
            }
        }
    }
}

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
     * 获取帖子这个动作的状态
     */
    val getPosterState by vm.getPosterStateFlow.collectAsState()

    /**
     * 加载更多评论这个动作的状态
     */
    val loadMoreState by vm.commentState.loadMoreStateFlow.collectAsState() // 加载更多评论

    /**
     * 刷新评论这个动作的状态
     */
    val refreshState by vm.commentState.refreshStateFlow.collectAsState() // 刷新

    /**
     * 对帖子的评论的编辑数据
     */
    val commentEditData by vm.commentEditDataFlow.collectAsState()

    /**
     * 子评论的刷新状态
     */
    val subCommentsRefreshState by vm.subCommentState.refreshStateFlow.collectAsState()

    /**
     * 子评论的加载更多状态
     */
    val subCommentsLoadMoreState by vm.subCommentState.loadMoreStateFlow.collectAsState()

    /**
     * 子评论
     */
    val subComments by vm.subCommentState.dataFlow.collectAsState()

    /**
     * 所有的评论
     */
    val comments by vm.commentState.dataFlow.collectAsState()

    /**
     * 通过bottom sheet展开的评论
     */
    val showComment by vm.showMoreStateFlow.collectAsState()

    /**
     * 所有正在进行点赞操作的帖子ID
     */
    val posterLikings by vm.posterLikeStatesFlow.collectAsState()

    /**
     * 所有正在进行点赞操作的评论ID
     */
    val commentLikings by vm.commentLikeStatesFlow.collectAsState()

    /**
     * 对评论的评论的编辑数据（一个Map，ID->CommentEditData）
     */
    val commentForCommentEditDataMap by vm.commentForCommentEditDataMapFlow.collectAsState()

    /**
     * 需要对其进行评论的评论，如果为null，那么不显示对话框
     */
    val sendCommentDialogState by vm.showCommentDialogStateLiveData.observeAsState()

    /**
     * 用于在对帖子进行评论的时候上传图片的启动器
     */
    val imagePickerLauncherForPoster = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val uri = data.data
                if (uri != null) vm.uploadImage(ctx, uri)
            }
        }
    }

    /**
     * 用于在对评论进行评论的时候上传图片的启动器
     */
    val imagePickerLauncherForComment = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val uri = data.data
                if (uri != null && sendCommentDialogState != null) {

                    val comment = sendCommentDialogState!!.first
                    val subComment = sendCommentDialogState!!.second

                    vm.uploadImageForComment(ctx, comment, subComment, uri)
                }
            }
        }
    }

    /**
     * 上传图片时发送的intent
     */
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
    }


    /**
     * 当获取帖子的状态为null的时候，获取帖子
     */
    LaunchedEffect(getPosterState) {
        if(getPosterState == null) {
            vm.getPosterById(id)
        }
    }

    /**
     * 当刷新评论的状态为null的时候，刷新评论
     */
    LaunchedEffect(refreshState) {
        if(refreshState == null) {
            vm.refreshComments(id)
        }
    }

    /**
     * 确认删除poster的对话框
     */
    var deletePosterDialogState by remember { mutableIntStateOf(-1) }

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
    var deleteCommentDialogState by remember { mutableIntStateOf(-1) }

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
            vm.refreshComments(id)
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
            vm.getSubComments(showComment.second!!.toLong())
        }
        onDispose {  }
    }

    /**
     * 确认删除对评论的评论中的图片的对话框
     */
    var deleteImageOfCommentDialogState by remember { mutableStateOf<Pair<Pair<Comment, Comment>, Int>?>(null) }

    /**
     * 确认删除对帖子的评论中的图片的对话框
     */
    var deleteImageOfPosterDialogState by remember { mutableIntStateOf(-1) }

    /**
     * 发送对评论的评论这个动作的状态
     */
    val sendCommentToCommentState by vm.sendCommentToCommentStateFlow.collectAsState()
    var needShowSnackbarForCommentToComment by remember { mutableStateOf(false) }

    /**
     * 当发送对评论的评论的状态为Success时，显示Snackbar
     */
    LaunchedEffect(sendCommentToCommentState, needShowSnackbarForCommentToComment) {
        if(sendCommentToCommentState is SendCommentState.Success && needShowSnackbarForCommentToComment) {
            mainController.snackbar("评论成功被发出去了！")
            needShowSnackbarForCommentToComment = false
        } else if(sendCommentToCommentState is SendCommentState.Fail && needShowSnackbarForCommentToComment) {
            mainController.snackbar("评论失败Orz")
            needShowSnackbarForCommentToComment = false
        }
    }

    /**
     * 发送对帖子的评论这个动作的状态
     */
    val sendCommentToPosterState by vm.sendCommentToPosterStateFlow.collectAsState()
    var needShowSnackbarForCommentToPoster by remember { mutableStateOf(false) }

    /**
     * 当发送对帖子的评论的状态为Success时，显示Snackbar
     */
    LaunchedEffect(sendCommentToPosterState, needShowSnackbarForCommentToPoster) {
        if(sendCommentToPosterState is SendCommentState.Success && needShowSnackbarForCommentToPoster) {
            mainController.snackbar("评论成功被发出去了！")
            needShowSnackbarForCommentToPoster = false
        } else if(sendCommentToPosterState is SendCommentState.Fail && needShowSnackbarForCommentToPoster) {
            mainController.snackbar("评论失败Orz")
            needShowSnackbarForCommentToPoster = false
        }
    }


    if(getPosterState is GetPosterState.Loading || refreshState is RefreshState.Loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else if(getPosterState is GetPosterState.Success && refreshState is RefreshState.Success) {
        PosterContent(
            mainController = mainController,

            data = (getPosterState as GetPosterState.Success).poster,
            comments = comments,
            posterLiking = posterLikings.contains(id),
            commentLikings = commentLikings,
            commentEditData = commentEditData ?: CommentEditData(
                "",
                UploadImageData(false, emptyList()),
                false
            ),
            loading = loadMoreState is LoadMoreState.Loading,
            state = rememberLoadableLazyColumnWithoutPullRequestState(
                onLoadMore = { vm.loadMoreComments(id) }
            ),

            onLikePoster = { vm.likePoster(id) },
            onLikeComment = vm::likeComment,
            onEditComment = vm::editComment,
            onShowMoreComments = { vm.setShowMoreState(true, it.id.toLong()) },
            onUploadImage = { imagePickerLauncherForPoster.launch(intent) },
            onSendCommentToPoster = {
                needShowSnackbarForCommentToPoster = true
                vm.sendCommentToPoster(id, it)
            },

            onOpenImage = onOpenImage,
            onOpenImages = onOpenImages,
            onOpenCommentDialog = vm::setShowCommentDialogState,
            onOpenEdit = { mainController.navController.navigate("edit/$id") },
            onOpenDeleteCommentDialog = { deleteCommentDialogState = it.id },
            onOpenDeletePosterDialog = { deletePosterDialogState = id.toInt() },
            onOpenReportPoster = { mainController.navController.navigate("report/poster/$id") },
            onOpenReportComment = { mainController.navController.navigate("report/comment/${it.id}") },
            onOpenDeleteImageOfPosterDialog = { deleteImageOfPosterDialogState = it },
        )

        AnimatedVisibility(
            visible = showComment.first,
            enter = slideIn(
                initialOffset = { IntOffset(0, it.height) },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )
            ),
            exit = slideOut(
                targetOffset = { IntOffset(0, it.height) },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )
            )
        ) {
            if(showComment.second != null) {
                val commentId = showComment.second!!.toLong()
                val comment = vm.findCommentById(commentId)!!

                MoreCommentsSheet(
                    mainController = mainController,
                    comment = comment,
                    subComments = subComments,
                    commentLikings = commentLikings,
                    loading = subCommentsLoadMoreState is LoadMoreState.Loading,
                    refreshing = subCommentsRefreshState is RefreshState.Loading,
                    state = rememberLoadableLazyColumnWithoutPullRequestState(
                        onLoadMore = { vm.loadMoreSubComments(commentId) }
                    ),

                    onCancel = { vm.setShowMoreState(false, null) },
                    onOpenImages = onOpenImages,
                    onLikeComment = vm::likeComment,
                    onOpenCommentDialog = vm::setShowCommentDialogState,
                    onOpenDeleteCommentDialog = { deleteCommentDialogState = it.id },
                    onReport = { mainController.navController.navigate("report/comment/${it.id}") },
                )
            } else {
                Surface(modifier = Modifier.fillMaxSize()) {}
            }
        }

        BackHandler(showComment.first) {
            vm.setShowMoreState(false, null)
        }

        if(sendCommentDialogState != null) {
            val comment = sendCommentDialogState!!.first
            val subComment = sendCommentDialogState!!.second

            val commentEditDataForComment = commentForCommentEditDataMap[Pair(comment.id, subComment.id)] ?: CommentEditData(
                "",
                UploadImageData(false, emptyList()),
                false
            )

            SendCommentDialog(
                mainController = mainController,
                replyUser = subComment.user,
                commentEditData = commentEditDataForComment,
                sending = false,

                onSendComment = {
                    needShowSnackbarForCommentToComment = true
                    vm.sendCommentToComment(comment, subComment, it)
                },
                onEditComment = { vm.editCommentOfComment(comment, subComment, it) },
                onOpenImage = onOpenImage,
                onUploadImage = { imagePickerLauncherForComment.launch(intent) },
                onClose = { vm.setShowCommentDialogState(null, null) },
                onOpenDeleteImageDialog = { deleteImageOfCommentDialogState = Pair(Pair(comment, subComment), it) }
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

        if(deleteImageOfCommentDialogState != null) {
            val comment = deleteImageOfCommentDialogState!!.first.first
            val subComment = deleteImageOfCommentDialogState!!.first.second
            val index = deleteImageOfCommentDialogState!!.second
            DeleteImageDialog(
                onConfirm = { vm.deleteImageOfCommentByIndex(comment, subComment, index) },
                onDismiss = { deleteImageOfCommentDialogState = null }
            )
        }

        if(deleteImageOfPosterDialogState != -1) {
            DeleteImageDialog(
                onConfirm = { vm.deleteImageOfPosterByIndex(deleteImageOfPosterDialogState) },
                onDismiss = { deleteImageOfPosterDialogState = -1 }
            )
        }
    } else {

    }
}