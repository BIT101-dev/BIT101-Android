package cn.bit101.android.features.setting.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.component.Avatar
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.GallerySettingData
import cn.bit101.android.features.setting.viewmodel.GalleryViewModel
import cn.bit101.api.model.common.User

@Composable
private fun GallerySettingPageContent(
    hideUserCount: Int,
    hideAnonymous: Boolean,
    settingData: GallerySettingData,

    onOpenHideUserListDialog: () -> Unit,
    onHideAnonymousChange: () -> Unit,
    onSettingChange: (GallerySettingData) -> Unit,
) {
    val hideSettings = listOf(
        SettingItemData.Switch(
            title = "在话廊中隐藏机器人 Poster",
            subTitle = "搜索栏、关注列表中的不受影响. 理论效果和网页端相同.",
            onClick = { onSettingChange(settingData.copy(hideBotPoster = it)) },
            checked = settingData.hideBotPoster,
        ),
        SettingItemData.Switch(
            title = "也隐藏搜索栏中的机器人 Poster",
            subTitle = "未开启隐藏机器人 Poster 时不生效",
            onClick = { onSettingChange(settingData.copy(hideBotPosterInSearch = it)) },
            checked = settingData.hideBotPosterInSearch,
            enable = settingData.hideBotPoster,
        ),
        SettingItemData.Button(
            title = "隐藏用户",
            subTitle = "查看被隐藏的非匿名用户列表.\n列表保存在本地, 和网页端不同步. 过滤操作在本地进行, 可能略微减慢加载速度.\n当前列表中${if (hideUserCount == 0) "没有用户" else "有 $hideUserCount 名用户"}",
            onClick = onOpenHideUserListDialog,
        ),
        SettingItemData.Switch(
            title = "隐藏匿名用户",
            subTitle = "功能同隐藏用户.\n因匿名用户发言占比相当大, 可能显著降低加载速度",
            onClick = { onHideAnonymousChange() },
            checked = hideAnonymous,
        ),
    )

    val viewSettings = listOf(
        SettingItemData.Switch(
            title = "允许横向滑动",
            subTitle = "可以横向滑动来切换选项卡, 可能导致误触",
            onClick = { onSettingChange(settingData.copy(allowHorizontalScroll = it)) },
            checked = settingData.allowHorizontalScroll,
        ),
    )

    SettingsColumn {
        SettingsGroup(
            title = "屏蔽设置",
            items = hideSettings
        )
        SettingsGroup(
            title = "浏览设置",
            items = viewSettings
        )
    }
}

@Composable
private fun HideUsersDialog(
    users: List<User>,
    isLoadingUsers: Boolean,

    onReshow: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        onDismissRequest = onDismiss,
        title = { Text(text = "隐藏用户列表") },
        text = {
            Column(Modifier.fillMaxSize()) {
                val scrollState = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    if (isLoadingUsers) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 5.dp),
                        )
                    } else {
                        users.forEachIndexed { index, user ->
                            if (user.id != -1) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp, 5.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(15.dp, 15.dp, 5.dp, 15.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Avatar(
                                            user = user,
                                            low = true,
                                            size = 45.dp,
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = user.nickname,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                text = user.motto,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        TextButton(onClick = { onReshow(index) }) {
                                            Text(text = "显示")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("你不会在话廊模块中看到其中用户发表的内容 (但仍能在“关注”页中看到其帖子).\n在对应用户的个人页面中隐藏用户, 对方无法直接得知自己被隐藏.\n* 为减少不必要的请求数目, 外部看到的评论、回复数仍是原值.")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("返回")
            }
        },
    )
}

@Composable
internal fun GallerySettingPage(
    onSnackBar: (String) -> Unit
) {
    val vm: GalleryViewModel = hiltViewModel()

    val settingData by vm.settingDataFlow.collectAsState(initial = GallerySettingData.default)
    val hideUserUids by vm.hideUserUidsFlow.collectAsState(initial = emptyList())

    var showHideUsersDialog by rememberSaveable { mutableStateOf(false) }

    if(showHideUsersDialog) {
        val hideUserInfos by vm.hideUserInfosFlow.observeAsState()

        LaunchedEffect(hideUserInfos) {
            if(hideUserInfos == null) {
                vm.getHideUserInfos()
            } else if (hideUserInfos is SimpleDataState.Fail) {
                onSnackBar("加载失败Orz")
            }
        }

        HideUsersDialog(
            users = (hideUserInfos as? SimpleDataState.Success)?.data ?: emptyList(),
            isLoadingUsers = hideUserInfos is SimpleDataState.Loading,
            onReshow = {
                vm.reshowUser(it)
            },
            onDismiss = { showHideUsersDialog = false },
        )
    }

    val hideAnonymous by remember{ derivedStateOf { hideUserUids.isNotEmpty() && hideUserUids.first() == -1 } }

    GallerySettingPageContent(
        hideUserCount = hideUserUids.size - (if(hideAnonymous) 1 else 0),
        hideAnonymous = hideAnonymous,
        settingData = settingData,
        onHideAnonymousChange = vm::switchShowAnonymous,
        onOpenHideUserListDialog = { showHideUsersDialog = true },
        onSettingChange = vm::setSettingData
    )
}