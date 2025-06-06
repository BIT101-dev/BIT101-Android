package cn.bit101.android.features.poster

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.Avatar
import cn.bit101.android.features.common.component.CustomDivider
import cn.bit101.android.features.common.component.gallery.AnnotatedText
import cn.bit101.android.features.common.component.gallery.CommentCard
import cn.bit101.android.features.common.component.gallery.LikeIcon
import cn.bit101.android.features.common.component.image.PreviewImagesWithGridLayout
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequest
import cn.bit101.android.features.common.component.loadable.LoadableLazyColumnWithoutPullRequestState
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.common.utils.DateTimeUtils
import cn.bit101.android.features.common.utils.NumberUtils
import cn.bit101.android.features.poster.component.CommentHeader
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.bit101.GetPosterDataModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PosterScreenTopBar(
    mainController: MainController,
    state: LoadableLazyColumnWithoutPullRequestState,
    data: GetPosterDataModel.Response,
    scrollBehavior: TopAppBarScrollBehavior,

    onMoreAction: () -> Unit,
) {
    TopAppBar(
        title = {
            // 如果目前在评论区，那么显示帖子的标题
            // 否则显示头像、昵称、身份
            val showTitle by remember { derivedStateOf { state.lazyListState.firstVisibleItemIndex >= 2} }

            AnimatedContent(
                targetState = showTitle,
                label = "top bar",
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                }
            ) {
                if(it) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium,
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
                            onClick = { mainController.navigate(NavDest.User(data.user.id.toLong())) }
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
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
            IconButton(onClick = { mainController.popBackStack() }) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            IconButton(onClick = onMoreAction) {
                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "更多")
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun PosterScreenBottomBar(
    posterLiking: Boolean,
    data: GetPosterDataModel.Response,
    onOpenCommentToPoster: () -> Unit,
    onLikePoster: () -> Unit,
) {
    // 底部评论、点赞、举报等操作
    BottomAppBar(
        modifier = Modifier.height(64.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onOpenCommentToPoster() })
                },
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            ),
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
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
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { if (!posterLiking) onLikePoster() })
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            LikeIcon(
                modifier = Modifier.size(28.dp),
                like = data.like,
                liking = posterLiking,
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Text(text = NumberUtils.format(data.likeNum), style = MaterialTheme.typography.titleMedium)
        }
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PosterContent(
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
     * 当前的评论排序方式
     */
    commentsOrder: CommentsOrderWithName,

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
     * 对当前的帖子进行点赞
     */
    onLikePoster: () -> Unit,

    /**
     * 对评论点赞，需要传入评论的ID
     */
    onLikeComment: (Comment) -> Unit,

    /**
     * 选择评论排序方式，需要传入排序方式的值
     */
    onSelectCommentsOrder: (CommentsOrderWithName) -> Unit,

    /**
     * 显示更多评论，需要传入*需要显示子评论的评论*的数据
     */
    onShowMoreComments: (Comment) -> Unit,

    /**
     * 打开图片组，第一个参数是默认显示的图片序号，第二个参数是url列表
     */
    onOpenImages: (Int, List<Image>) -> Unit,

    /**
     * 打开*对评论的评论*的编辑对话框，第一个参数是主评论，第二个参数是子评论
     */
    onOpenCommentToComment: (Comment, Comment) -> Unit,

    /**
     * 打开*对帖子的评论*的编辑对话框
     */
    onOpenCommentToPoster: () -> Unit,

    /**
     * 打开评论的更多操作的bottom sheet，需要传入评论的数据
     */
    onOpenMoreActionOfCommentBottomSheet: (Comment) -> Unit,

    /**
     * 打开更多操作的bottom sheet
     */
    onOpenMoreActionOfPosterBottomSheet: () -> Unit
) {
    val cm = LocalClipboardManager.current

    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        topBar = {
            PosterScreenTopBar(
                mainController = mainController,
                state = state,
                data = data,
                scrollBehavior = topBarScrollBehavior,
                onMoreAction = onOpenMoreActionOfPosterBottomSheet,
            )
        },
        bottomBar = {
            PosterScreenBottomBar(
                posterLiking = posterLiking,
                data = data,
                onOpenCommentToPoster = onOpenCommentToPoster,
                onLikePoster = onLikePoster,
            )
        }
    ) { paddingValues ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LoadableLazyColumnWithoutPullRequest(
                modifier = Modifier.fillMaxWidth(),
                loading = loading,
                state = state,
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // 标题+声明+正文+图片+标签+最后编辑时间
                item(0) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                            )
                        }
                        if(data.claim.id != 0) {
                            Spacer(modifier = Modifier.padding(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "创作者声明：${data.claim.text}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    ),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.padding(6.dp))
                        if (data.text.isNotEmpty()) {
                            SelectionContainer {
                                AnnotatedText(
                                    text = data.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    onOpenPoster = { mainController.navigate(NavDest.Poster(it)) },
                                    onOpenUser = { mainController.navigate(NavDest.User(it)) }
                                )
                            }
                        }
                        if (data.images.isNotEmpty()) {
                            Spacer(modifier = Modifier.padding(2.dp))
                            PreviewImagesWithGridLayout(
                                modifier = Modifier.fillMaxWidth(),
                                images = data.images,
                                maxCountInEachRow = 3,
                                onClick = { onOpenImages(it, data.images) },
                            )
                        }
                        if (data.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.padding(8.dp))
                            FlowRow {
                                data.tags.forEach { tag ->
                                    Text(
                                        modifier = Modifier
                                            .pointerInput(Unit) {
                                                detectTapGestures(onTap = { mainController.copyText(cm, tag) })
                                            },
                                        text = "#$tag",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                        ),
                                    )
                                    Spacer(modifier = Modifier.padding(end = 6.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "最后编辑于：" + DateTimeUtils.format(DateTimeUtils.formatTime(data.updateTime)),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                item {
                    CustomDivider()
                }

                if(data.commentNum > 0) {
                    item(8) {
                        CommentHeader(
                            title = "评论 ${data.commentNum}",
                            commentsOrder = commentsOrder,
                            onSelectCommentsOrder = onSelectCommentsOrder,
                        )
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
                            onOpenCommentToComment = onOpenCommentToComment,
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
                            Spacer(modifier = Modifier.padding(16.dp))
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
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(45.dp),
                                imageVector = Icons.Rounded.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )

                            Spacer(modifier = Modifier.padding(4.dp))

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
        }
    }
}