package cn.bit101.android.ui.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.ui.component.common.CustomDivider
import cn.bit101.android.ui.component.user.UserInfoContent
import cn.bit101.android.ui.component.user.UserInfoContentForMe

@Composable
fun UserInfoShow(
    mainController: MainController,
    state: UpdateUserInfoState?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        onClick = {
            if (state is UpdateUserInfoState.Success) {
                mainController.navController.navigate("user/${state.user.user.id}")
            }
        },
    ) {
        if(state is UpdateUserInfoState.Success) {
            Box(modifier = Modifier.padding(10.dp)) {
                UserInfoContentForMe(
                    mainController = mainController,
                    data = state.user,
                    onOpenFollowerDialog = {},
                    onOpenFollowingDialog = {},
                )
            }
        } else {
            Text(
                text = "请先登录awa",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    mainController: MainController,
    vm: MineViewModel = hiltViewModel(),
) {
    val userInfoState by vm.updateUserInfoStateLiveData.observeAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        vm.updateUserInfo()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .height(84.dp)
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(imageVector = Icons.Rounded.NotificationsNone, contentDescription = "通知")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {

                // 用户基本信息展示
                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                    if (userInfoState is UpdateUserInfoState.Success) {
                        UserInfoContentForMe(
                            mainController = mainController,
                            data = (userInfoState as UpdateUserInfoState.Success).user,
                            onOpenFollowerDialog = {},
                            onOpenFollowingDialog = {},
                        )
                    } else {
                        Text(
                            text = "请先登录awa",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                CustomDivider(
                    height = 8.dp
                )

                // 设置选项
                ConfigColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    scrollable = false,
                    items = listOf(
                        ConfigItem.Button(
                            title = "登陆管理",
                            onClick = {
                                mainController.navigate("login") {
                                    launchSingleTop = true
                                }
                            }
                        ),
                        ConfigItem.Button(
                            title = "设置",
                            onClick = { mainController.navigate("setting?route=") }
                        ),
                        ConfigItem.Button(
                            title = "成绩查询",
                            onClick = { mainController.openWebPage("https://bit101.cn/score") }
                        )
                    )
                )
            }
        }
    }


}