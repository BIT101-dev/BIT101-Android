package cn.bit101.android.features.mine

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.user.UserInfoContentForMe
import cn.bit101.android.features.common.helper.CourseUrl
import cn.bit101.android.features.common.helper.PaperUrl
import cn.bit101.android.features.common.helper.ScoreUrl
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import kotlinx.coroutines.launch


private data class FunctionItem(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MineScreenContent(
    mainController: MainController,
    messageCount: Int,
    userInfoState: SimpleDataState<GetUserInfoDataModel.Response>,
    onRefresh: () -> Unit,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit,
    onOpenPostersDialog: () -> Unit,
    onOpenMessagePage: () -> Unit,
) {
    val cm = LocalClipboardManager.current

    val scrollState = rememberScrollState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val functions = listOf(
        FunctionItem(
            name = "成绩",
            icon = Icons.Outlined.School,
            onClick = { mainController.openWebPage(ScoreUrl) }
        ),
        FunctionItem(
            name = "文章",
            icon = Icons.AutoMirrored.Outlined.Article,
            onClick = { mainController.openWebPage(PaperUrl) }
        ),
        FunctionItem(
            name = "课程",
            icon = Icons.Outlined.Book,
            onClick = { mainController.openWebPage(CourseUrl) }
        ),
    )

    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val viewWidth = LocalDensity.current.run {
        LocalView.current.measuredWidth.toDp()
    }

    // 解决横屏时抽屉宽度过大的问题
    val drawerMaxWidth = minOf(viewWidth * 0.6f, DrawerDefaults.MaximumDrawerWidth)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerMaxWidth),
            ){
                LazyColumn {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                            text = "其他功能",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Divider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                    items(functions) {
                        ListItem(
                            modifier = Modifier.clickable { it.onClick() },
                            leadingContent = { Icon(imageVector = it.icon, contentDescription = null) },
                            headlineContent = { Text(text = it.name) },
                        )
                    }
                }
            }
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "刷新")
                        }

                        if(messageCount > 0) {
                            Box {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp),
                                ) {
                                    Text(text = messageCount.toString())
                                }
                                IconButton(onClick = onOpenMessagePage) {
                                    Icon(
                                        imageVector = Icons.Outlined.NotificationsActive,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        contentDescription = "通知"
                                    )
                                }
                            }

                        } else {
                            IconButton(onClick = onOpenMessagePage) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsNone,
                                    contentDescription = "通知"
                                )
                            }
                        }

                        IconButton(onClick = { mainController.navigate(NavDest.Setting()) }) {
                            Icon(imageVector = Icons.Outlined.Settings, contentDescription = "通知")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Outlined.Menu, contentDescription = "侧边抽屉")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val data = (userInfoState as? SimpleDataState.Success)?.data
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    UserInfoContentForMe(
                        data = data,
                        onOpenMineIndex = {
                            (userInfoState as? SimpleDataState.Success)?.data?.user?.id?.let { id ->
                                mainController.navigate(NavDest.User(id.toLong()))
                            }
                        },
                        onOpenFollowerDialog = onOpenFollowerDialog,
                        onOpenFollowingDialog = onOpenFollowingDialog,
                        onOpenPostersDialog = onOpenPostersDialog,
                        onCopyText = { mainController.copyText(cm, it) },
                        onShowImage = { mainController.showImage(it) },
                        onOpenPoster = { mainController.navigate(NavDest.Poster(it)) },
                        onOpenUser = { mainController.navigate(NavDest.User(it)) },
                    )
                    Spacer(modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
