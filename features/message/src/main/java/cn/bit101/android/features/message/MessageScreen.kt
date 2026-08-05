package cn.bit101.android.features.message

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.Avatar
import cn.bit101.android.features.common.component.CircularProgressIndicatorForPage
import cn.bit101.android.features.common.component.ErrorMessageForPage
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.common.utils.DateTimeUtils
import cn.bit101.api.model.common.MessageType
import cn.bit101.api.model.http.bit101.GetMessagesDataModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val messageTypes = listOf(
    MessageType.LIKE to "点赞",
    MessageType.COMMENT to "评论",
    MessageType.FOLLOW to "关注",
    MessageType.SYSTEM to "系统",
)

private val messageTypeIcons = mapOf(
    MessageType.LIKE to Icons.Outlined.ThumbUp,
    MessageType.COMMENT to Icons.Outlined.ModeComment,
    MessageType.FOLLOW to Icons.Outlined.PersonAdd,
    MessageType.SYSTEM to Icons.Outlined.Campaign,
)

private fun objLabel(obj: String?): String? = when {
    obj.isNullOrEmpty() -> null
    obj.startsWith("poster") -> "帖子"
    obj.startsWith("comment") -> "评论"
    obj.startsWith("user") -> "用户"
    else -> obj
}

private fun actionText(type: String, obj: String?): String = when (type) {
    MessageType.LIKE -> "赞了你的${objLabel(obj) ?: "内容"}"
    MessageType.COMMENT -> "评论了你的${objLabel(obj) ?: "内容"}"
    MessageType.FOLLOW -> "关注了你"
    MessageType.SYSTEM -> {
        val label = objLabel(obj)
        if (label != null) "系统消息 · $label" else "系统消息"
    }
    else -> "新消息"
}

private fun readableTime(time: String): String =
    DateTimeUtils.formatTime(time)?.let { DateTimeUtils.calculateTimeDiff(it) } ?: time

private fun parseTarget(id: String?): NavDest? {
    if (id.isNullOrEmpty()) return null
    return when {
        id.startsWith("poster") -> id.removePrefix("poster").toLongOrNull()?.let { NavDest.Poster(it) }
        id.startsWith("user") -> id.removePrefix("user").toLongOrNull()?.let { NavDest.User(it) }
        else -> null
    }
}

private fun messageNavDest(type: String, message: GetMessagesDataModel.ResponseItem): NavDest? {
    parseTarget(message.linkObj)?.let { return it }
    parseTarget(message.obj)?.let { return it }
    return if (type == MessageType.FOLLOW) NavDest.User(message.fromUser.id.toLong()) else null
}

@Composable
private fun MessageItem(
    type: String,
    message: GetMessagesDataModel.ResponseItem,
    onClick: () -> Unit,
) {
    val target = messageNavDest(type, message)
    val timeText = remember(message.updateTime) { readableTime(message.updateTime) }
    val icon = messageTypeIcons[type]
    val action = actionText(type, message.obj)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Avatar(user = message.fromUser, size = 44.dp)
        Spacer(modifier = Modifier.padding(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (message.fromUser.nickname != "") {
                Text(
                    text = message.fromUser.nickname,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.padding(2.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                }
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            if (message.text.isNotBlank()) {
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.outline
                ),
            )
        }
        if (target != null) {
            Spacer(modifier = Modifier.padding(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun MessageTypePage(
    type: String,
    state: SimpleDataState<List<GetMessagesDataModel.ResponseItem>>?,
    loadMoreState: SimpleState?,
    onLoadMore: () -> Unit,
    onItemClick: (GetMessagesDataModel.ResponseItem) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount
            lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd -> if (nearEnd) onLoadMore() }
    }

    when (state) {
        is SimpleDataState.Loading -> CircularProgressIndicatorForPage()
        is SimpleDataState.Fail -> ErrorMessageForPage()
        is SimpleDataState.Success -> {
            val messages = state.data
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "暂无消息",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                ) {
                    items(messages.size) { index ->
                        val message = messages[index]
                        MessageItem(
                            type = type,
                            message = message,
                            onClick = { onItemClick(message) },
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp,
                        )
                    }
                    if (loadMoreState is SimpleState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
        null -> CircularProgressIndicatorForPage()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MessageScreen(mainController: MainController) {
    val vm: MessageViewModel = hiltViewModel()

    val unreadCount by vm.unreadCountStateFlow.collectAsState()
    val separateUnreadCount by vm.separateUnreadCountStateFlow.collectAsState()
    val messagesStateByType by vm.messagesStateByTypeFlow.collectAsState()
    val loadMoreStateByType by vm.loadMoreStateByTypeFlow.collectAsState()

    val pagerState = rememberPagerState(pageCount = { messageTypes.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        vm.loadUnreadCounts()
        vm.refreshMessages()
    }

    LaunchedEffect(pagerState.settledPage) {
        vm.selectType(messageTypes[pagerState.settledPage].first)
    }

    val separateCount = (separateUnreadCount as? SimpleDataState.Success)?.data
    val totalUnread = (unreadCount as? SimpleDataState.Success)?.data

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "消息",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    IconButton(onClick = {
                        vm.loadUnreadCounts()
                        vm.refreshMessages()
                    }) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "刷新")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { mainController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                messageTypes.forEachIndexed { index, (type, label) ->
                    val unread = separateCount?.let {
                        when (type) {
                            MessageType.SYSTEM -> it.system
                            MessageType.COMMENT -> it.comment
                            MessageType.LIKE -> it.like
                            else -> it.follow
                        }
                    }
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.scrollToPage(index) }
                        },
                        text = {
                            BadgedBox(
                                badge = {
                                    if (unread != null && unread > 0) {
                                        Badge { Text(text = unread.toString()) }
                                    }
                                }
                            ) {
                                Text(text = label)
                            }
                        },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                val type = messageTypes[index].first
                MessageTypePage(
                    type = type,
                    state = messagesStateByType[type],
                    loadMoreState = loadMoreStateByType[type],
                    onLoadMore = vm::loadMoreMessages,
                    onItemClick = { message ->
                        messageNavDest(type, message)?.let { mainController.navigate(it) }
                    },
                )
            }
        }
    }
}
