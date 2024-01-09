package cn.bit101.android.ui.mine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Score
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.CourseUrl
import cn.bit101.android.ui.common.PaperUrl
import cn.bit101.android.ui.common.ScoreUrl
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.component.pullrefresh.PullRefreshIndicator
import cn.bit101.android.ui.component.pullrefresh.pullRefresh
import cn.bit101.android.ui.component.pullrefresh.rememberPullRefreshState
import cn.bit101.android.ui.component.user.UserInfoContentForMe
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import kotlinx.coroutines.launch


private data class FunctionItem(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun Functions(
    mainController: MainController,
) {
    val functions = listOf(
        FunctionItem(
            name = "成绩",
            icon = Icons.Rounded.School,
            onClick = { mainController.openWebPage(ScoreUrl) }
        ),
        FunctionItem(
            name = "文章",
            icon = Icons.AutoMirrored.Rounded.Article,
            onClick = { mainController.openWebPage(PaperUrl) }
        ),
        FunctionItem(
            name = "课程",
            icon = Icons.Rounded.Book,
            onClick = { mainController.openWebPage(CourseUrl) }
        ),
    )

    val countEachRow = 4
    val heightEachRow = 28.dp + 2.dp + 16.dp + 12.dp * 2
    val rows = if(functions.size % countEachRow == 0) functions.size / countEachRow else functions.size / countEachRow + 1
    val height = heightEachRow * rows + 1.dp * (rows + 1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .padding(vertical = 18.dp)
                .height(height),
            columns = GridCells.Fixed(countEachRow),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(functions) { item ->
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { item.onClick() })
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = item.icon,
                        contentDescription = item.name,
                    )
                    Spacer(modifier = Modifier.padding(1.dp))
                    Text(
                        modifier = Modifier.height(16.dp),
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreenContent(
    mainController: MainController,
    userInfoState: SimpleDataState<GetUserInfoDataModel.Response>,
    onRefresh: () -> Unit,
    onOpenFollowerDialog: () -> Unit,
    onOpenFollowingDialog: () -> Unit
) {
    val cm = LocalClipboardManager.current

    val scrollState = rememberScrollState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val functions = listOf(
        FunctionItem(
            name = "成绩",
            icon = Icons.Rounded.School,
            onClick = { mainController.openWebPage(ScoreUrl) }
        ),
        FunctionItem(
            name = "文章",
            icon = Icons.AutoMirrored.Rounded.Article,
            onClick = { mainController.openWebPage(PaperUrl) }
        ),
        FunctionItem(
            name = "课程",
            icon = Icons.Rounded.Book,
            onClick = { mainController.openWebPage(CourseUrl) }
        ),
    )

    val pullRefreshState = rememberPullRefreshState(
        refreshing = userInfoState is SimpleDataState.Loading,
        onRefresh = onRefresh
    )

    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.6f),
            ){
                LazyColumn {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                            text = "其他功能",
                            style = MaterialTheme.typography.titleMedium
                        )
                        HorizontalDivider(
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
                        IconButton(
                            onClick = { /*TODO*/ }
                        ) {
                            Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = "通知")
                        }
                        IconButton(onClick = { mainController.navigate("setting?route=") }) {
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
                                mainController.navigate("user/$id")
                            }
                        },
                        onOpenFollowerDialog = onOpenFollowerDialog,
                        onOpenFollowingDialog = onOpenFollowingDialog,
                        onCopyText = { mainController.copyText(cm, it) },
                        onShowImage = { mainController.showImage(it) },
                        onOpenPoster = { mainController.navigate("poster/$it") },
                        onOpenUser = { mainController.navigate("user/$it") },
                    )
                    Spacer(modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
