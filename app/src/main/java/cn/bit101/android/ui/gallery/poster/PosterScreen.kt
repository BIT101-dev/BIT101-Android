package cn.bit101.android.ui.gallery.poster

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.ui.component.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.component.PreviewImagesWithGridLayout
import cn.bit101.android.ui.component.rememberLoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.UploadImageData
import cn.bit101.android.ui.component.gallery.CommentCard
import cn.bit101.android.ui.component.gallery.AnnotatedText
import cn.bit101.android.ui.component.gallery.DeleteCommentDialog
import cn.bit101.android.ui.component.gallery.DeleteImageDialog
import cn.bit101.android.ui.component.gallery.DeletePosterDialog
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.android.utils.NumberUtils
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPosterDataModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosterScreenTopBar(
    mainController: MainController,
    state: LoadableLazyColumnWithoutPullRequestState,
    data: GetPosterDataModel.Response,
) {
    TopAppBar(
        title = {
            // 如果目前在评论区，那么显示帖子的标题
            // 否则显示头像、昵称、身份
            val showTitle by remember { derivedStateOf { state.lazyListState.firstVisibleItemIndex >= 7} }

            AnimatedContent(
                targetState = showTitle,
                label = "top bar",
            ) {
                if(it) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Row(
                        modifier = Modifier.wrapContentSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            user = data.user,
                            low = true,
                            size = 32.dp,
                            onClick = { mainController.navController.navigate("user/${data.user.id}") }
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = data.user.nickname,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = data.user.identity.text,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if(data.user.identity.id == 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    else Color(android.graphics.Color.parseColor(data.user.identity.color))
                                ),
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { mainController.navController.popBackStack() }) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            IconButton(
                onClick = {}
            ) {
                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "更多")
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PosterContent(
    mainController: MainController,

    /**
     * 是否正在加载评论
     */
    loading: Boolean,

    /**
     * 是否已经加载完所有的评论
     */
    loaded: Boolean,

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
     * 打开对帖子的删除对话框
     */
    onOpenDeletePosterDialog: () -> Unit,

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

    /**
     * 打开评论的更多操作的bottom sheet，需要传入评论的数据
     */
    onOpenMoreActionOfCommentBottomSheet: (Comment) -> Unit,
) {
    val cm = LocalClipboardManager.current

    val bottomContentHeight = 60.dp

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PosterScreenTopBar(
                mainController = mainController,
                state = state,
                data = data,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // poster内容卡片
            LoadableLazyColumnWithoutPullRequest(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomContentHeight),
                loading = loading,
                state = state,
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // 标题
                item(-1) {
                    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                            )
                        }
                    }
                }

                item(0) {
                    if(data.claim.id != 0) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = data.claim.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    ),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.padding(2.dp))
                    }
                    Spacer(modifier = Modifier.padding(2.dp))
                }

                // 内容
                item(4) {
                    // 正文
                    if (data.text.isNotEmpty()) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
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
                }

                // 图片
                item(5) {
                    if (data.images.isNotEmpty()) {
                        Spacer(modifier = Modifier.padding(2.dp))
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            PreviewImagesWithGridLayout(
                                modifier = Modifier.fillMaxWidth(),
                                images = data.images,
                                maxCountInEachRow = 3,
                                onClick = { onOpenImages(it, data.images) },
                            )
                        }
                    }
                }

                // 标签
                item(6) {
                    if (data.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.padding(8.dp))
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            FlowRow {
                                data.tags.forEach { tag ->
                                    Text(
                                        modifier = Modifier.clickable { mainController.copyText(cm, buildAnnotatedString { append(tag) }) },
                                        text = "#$tag",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                        ),
                                    )
                                    Spacer(modifier = Modifier.padding(end = 6.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                }

                item(12) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            text = "最后编辑于：" + DateTimeUtils.format(DateTimeUtils.formatTime(data.updateTime)),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                item {
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

                if(data.commentNum > 0) {
                    item(8) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                text = "共${data.commentNum}条评论",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.padding(4.dp))
                    }

                    // 评论展示
                    items(comments, { it.id + 100 }) { comment ->
                        CommentCard(
                            mainController = mainController,
                            comment = comment,
                            onOpenImage = onOpenImages,
                            onShowMoreComments = { onShowMoreComments(comment) },
                            commentLikings = commentLikings,
                            onLikeComment = onLikeComment,
                            onClick = { onOpenCommentDialog(comment, comment) },
                            onMoreAction = onOpenMoreActionOfCommentBottomSheet
                        )
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
                } else {
                    item(8) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(45.dp),
                                imageVector = Icons.Rounded.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )

                            Spacer(modifier = Modifier.padding(2.dp))

                            Text(
                                text = "这里没有评论呢",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.padding(2.dp))
                    }
                }
            }

            // 底部评论、点赞、举报等操作

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomContentHeight)
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier = Modifier
                            .height(bottomContentHeight - 15.dp)
                            .padding(end = 4.dp, start = 8.dp, top = 4.dp, bottom = 4.dp)
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { onOpenEdit() })
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                text = "快来评论吧",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .size(bottomContentHeight - 10.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { if (!posterLiking) onLikePoster() })
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = if(data.like) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "点赞",
                            tint = if(posterLiking) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else if(data.like) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = NumberUtils.format(data.likeNum), style = MaterialTheme.typography.labelSmall)
                    }

                    Column(
                        modifier = Modifier
                            .size(bottomContentHeight - 10.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { })
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "分享",
                        )
                        Text(text = "分享", style = MaterialTheme.typography.labelSmall)
                    }
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

    val cm = LocalClipboardManager.current

    val view = LocalView.current

    /**
     * 获取帖子这个动作的状态
     */
    val getPosterState by vm.getPosterStateFlow.collectAsState()

    /**
     * 加载更多评论这个动作的状态
     */
    val loadMoreState by vm.commentStateFlows.loadMoreStateFlow.collectAsState() // 加载更多评论

    /**
     * 刷新评论这个动作的状态
     */
    val refreshState by vm.commentStateFlows.refreshStateFlow.collectAsState() // 刷新

    /**
     * 对帖子的评论的编辑数据
     */
    val commentEditData by vm.commentEditDataFlow.collectAsState()

    /**
     * 子评论的刷新状态
     */
    val subCommentsRefreshState by vm.subCommentStateFlows.refreshStateFlow.collectAsState()

    /**
     * 子评论的加载更多状态
     */
    val subCommentsLoadMoreState by vm.subCommentStateFlows.loadMoreStateFlow.collectAsState()

    /**
     * 子评论
     */
    val subComments by vm.subCommentStateFlows.dataFlow.collectAsState()

    /**
     * 所有的评论
     */
    val comments by vm.commentStateFlows.dataFlow.collectAsState()

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
    var deleteImageOfCommentDialogState by rememberSaveable { mutableStateOf<Pair<Pair<Comment, Comment>, Int>?>(null) }

    /**
     * 确认删除对帖子的评论中的图片的对话框
     */
    var deleteImageOfPosterDialogState by rememberSaveable { mutableIntStateOf(-1) }

    /**
     * 发送对评论的评论这个动作的状态
     */
    val sendCommentToCommentState by vm.sendCommentToCommentStateFlow.collectAsState()
    var needShowSnackbarForCommentToComment by rememberSaveable { mutableStateOf(false) }

    /**
     * 当发送对评论的评论的状态为Success时，显示Snackbar
     */
    LaunchedEffect(sendCommentToCommentState, needShowSnackbarForCommentToComment) {
        if(sendCommentToCommentState is SimpleState.Success && needShowSnackbarForCommentToComment) {
            mainController.snackbar("评论成功被发出去了！")
            needShowSnackbarForCommentToComment = false
        } else if(sendCommentToCommentState is SimpleState.Fail && needShowSnackbarForCommentToComment) {
            mainController.snackbar("评论失败Orz")
            needShowSnackbarForCommentToComment = false
        }
    }

    /**
     * 发送对帖子的评论这个动作的状态
     */
    val sendCommentToPosterState by vm.sendCommentToPosterStateFlow.collectAsState()
    var needShowSnackbarForCommentToPoster by rememberSaveable { mutableStateOf(false) }

    /**
     * 当发送对帖子的评论的状态为Success时，显示Snackbar
     */
    LaunchedEffect(sendCommentToPosterState, needShowSnackbarForCommentToPoster) {
        if(sendCommentToPosterState is SimpleState.Success && needShowSnackbarForCommentToPoster) {
            mainController.snackbar("评论成功被发出去了！")
            needShowSnackbarForCommentToPoster = false
        } else if(sendCommentToPosterState is SimpleState.Fail && needShowSnackbarForCommentToPoster) {
            mainController.snackbar("评论失败Orz")
            needShowSnackbarForCommentToPoster = false
        }
    }


    var showMoreActionOfCommentState by remember { mutableStateOf<Comment?>(null) }


    val commentLoaded = vm.commentStateFlows.pageFlow.collectAsState().value == -1

    val subCommentLoaded = vm.subCommentStateFlows.pageFlow.collectAsState().value == -1


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
            posterLiking = posterLikings.contains(id),
            commentLikings = commentLikings,
            commentEditData = commentEditData ?: CommentEditData(
                "",
                UploadImageData(false, emptyList()),
                false
            ),
            loading = loadMoreState is SimpleState.Loading,
            loaded = commentLoaded,
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
            onOpenDeletePosterDialog = { deletePosterDialogState = id.toInt() },
            onOpenReportPoster = { mainController.navController.navigate("report/poster/$id") },
            onOpenDeleteImageOfPosterDialog = {
                deleteImageOfPosterDialogState = it
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            },
            onOpenMoreActionOfCommentBottomSheet = { showMoreActionOfCommentState = it },
        )

        if(showComment.first) {
            val commentId = showComment.second!!
            val comment = vm.findCommentById(commentId)!!
            MoreCommentsSheet(
                mainController = mainController,
                comment = comment,
                subComments = subComments,
                commentLikings = commentLikings,
                loading = subCommentsLoadMoreState is SimpleState.Loading,
                loaded = subCommentLoaded,
                refreshing = subCommentsRefreshState is SimpleState.Loading,
                state = rememberLoadableLazyColumnWithoutPullRequestState(
                    onLoadMore = { vm.loadMoreSubComments(commentId) }
                ),

                onCancel = { vm.setShowMoreState(false) },
                onOpenImages = onOpenImages,
                onLikeComment = vm::likeComment,
                onOpenCommentDialog = vm::setShowCommentDialogState,
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
                onOpenDeleteImageDialog = {
                    deleteImageOfCommentDialogState = Pair(Pair(comment, subComment), it)
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
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