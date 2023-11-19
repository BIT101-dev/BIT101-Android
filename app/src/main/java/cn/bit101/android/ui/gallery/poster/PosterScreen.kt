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
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Comment
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import cn.bit101.android.ui.gallery.component.CommentCard
import cn.bit101.android.ui.gallery.component.CommentEditContent
import cn.bit101.android.ui.gallery.component.AnnotatedText
import cn.bit101.android.ui.gallery.component.DeleteCommentDialog
import cn.bit101.android.ui.gallery.component.DeleteImageDialog
import cn.bit101.android.ui.gallery.component.DeletePosterDialog
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPosterDataModel
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PosterContent(
    mainController: MainController,

    loading: Boolean,
    comments: List<Comment>,
    state: LoadableLazyColumnWithoutPullRequestState,
    commentLikes: Set<Long>,
    posterLikes: Set<Long>,
    data: GetPosterDataModel.Response,
    commentEditData: CommentEditData,

    onLikePoster: (Long) -> Unit,
    onEditComment: (CommentEditData) -> Unit,
    onLikeComment: (Long) -> Unit,
    onShowMoreComments: (Comment) -> Unit,
    onSendCommentToPoster: (CommentEditData) -> Unit,
    onUploadImage: () -> Unit,

    onOpenReportPoster: () -> Unit,
    onOpenReportComment: (Comment) -> Unit,
    onOpenDeletePosterDialog: () -> Unit,
    onOpenDeleteCommentDialog: (Comment) -> Unit,
    onOpenImage: (Image) -> Unit,
    onOpenEdit: (Long) -> Unit,
    onOpenCommentDialog: (Comment, Comment) -> Unit,
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

            // 帖子信息
            item(3) {
                Column {
                    if(data.own && !data.public) {
                        Text(
                            text = "仅自己可见",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.padding(1.dp))
                    }
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
                    Text(
                        text = "首次创建时间：" + DateTimeUtils.format(DateTimeUtils.formatTime(data.createTime)),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.padding(1.dp))
                    Text(
                        text = "最后修改时间：" + DateTimeUtils.format(DateTimeUtils.formatTime(data.updateTime)),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
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
                            val urls = data.images.map { URLEncoder.encode(it.url, "UTF-8") }
                            PreviewImage(
                                image = image,
                                onClick = { mainController.navController.navigate("images/$urls/${index}") },
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
                        IconButton(onClick = { onOpenEdit(data.id.toLong()) }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "编辑",
                            )
                        }
                    }
                    // 点赞
                    BadgedBox(
                        badge = {
                            if(data.likeNum > 0) Badge { Text(text = data.likeNum.toString()) }
                        }
                    ) {
                        val liking = posterLikes.contains(data.id.toLong())
                        if(data.like) {
                            IconButton(
                                onClick = {
                                    onLikePoster(data.id.toLong())
                                },
                                colors = IconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = MaterialTheme.colorScheme.tertiary,
                                ),
                                enabled = !liking
                            ) {
                                if(liking) {
                                    CircularProgressIndicator()
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.ThumbUp,
                                        contentDescription = "点赞",
                                    )
                                }
                            }

                        } else {
                            IconButton(
                                onClick = {
                                    onLikePoster(data.id.toLong())
                                },
                                enabled = !liking
                            ) {
                                if(liking) {
                                    CircularProgressIndicator()
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.ThumbUp,
                                        contentDescription = "点赞",
                                    )
                                }
                            }
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
            if(comments.isNotEmpty()) {
                comments.forEach { comment ->
                    item(comment.id + 100) {
                        CommentCard(
                            mainController = mainController,
                            comment = comment,
                            onOpenImage = { index ->
                                val urls = comment.images.map { URLEncoder.encode(it.url, "UTF-8") }
                                mainController.navController.navigate("images/$urls/${index}")
                            },
                            onShowMoreComments = { onShowMoreComments(comment) },
                            commentLikes = commentLikes,
                            onLikeComment = { onLikeComment(comment.id.toLong()) },
                            onClick = { onOpenCommentDialog(comment, comment) },
                            onReport = { onOpenReportComment(comment) },
                            onOpenDeleteCommentDialog = { onOpenDeleteCommentDialog(comment) },
                        )
                    }
                }
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
    val posterLikes by vm.posterLikeStatesFlow.collectAsState()

    /**
     * 所有正在进行点赞操作的评论ID
     */
    val commentLikes by vm.commentLikeStatesFlow.collectAsState()

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
        } else if(deletePosterState is SimpleState.Error) {
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
        } else if (deleteCommentState is SimpleState.Error) {
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when(getPosterState) {
            // 失败
            null, is GetPosterState.Fail -> {

            }
            // 正在加载
            is GetPosterState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .width(64.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is GetPosterState.Success -> {
                PosterContent(
                    mainController = mainController,

                    data = (getPosterState as GetPosterState.Success).poster,
                    comments = comments,
                    posterLikes = posterLikes,
                    commentLikes = commentLikes,
                    commentEditData = commentEditData ?: CommentEditData(
                        "",
                        UploadImageData(false, emptyList()),
                        false
                    ),
                    loading = loadMoreState is LoadMoreState.Loading,
                    state = rememberLoadableLazyColumnWithoutPullRequestState(
                        onLoadMore = { vm.loadMoreComments(id) }
                    ),

                    onLikePoster = vm::likePoster,
                    onLikeComment = vm::likeComment,
                    onEditComment = vm::editComment,
                    onShowMoreComments = { vm.setShowMoreState(true, it.id.toLong()) },
                    onUploadImage = { imagePickerLauncherForPoster.launch(intent) },
                    onSendCommentToPoster = {
                        needShowSnackbarForCommentToPoster = true
                        vm.sendCommentToPoster(id, it)
                    },

                    onOpenImage = onOpenImage,
                    onOpenCommentDialog = vm::setShowCommentDialogState,
                    onOpenEdit = { mainController.navController.navigate("edit/$it") },
                    onOpenDeleteCommentDialog = { deleteCommentDialogState = it.id },
                    onOpenDeletePosterDialog = { deletePosterDialogState = id.toInt() },
                    onOpenReportPoster = { mainController.navController.navigate("report/poster/$id") },
                    onOpenReportComment = { mainController.navController.navigate("report/comment/${it.id}") },
                    onOpenDeleteImageOfPosterDialog = { deleteImageOfPosterDialogState = it }
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
                            commentLikes = commentLikes,
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
            }
        }
    }
}