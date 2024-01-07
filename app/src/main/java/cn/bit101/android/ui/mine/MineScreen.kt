package cn.bit101.android.ui.mine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
                mainController.navController.navigate("user/0")
            }
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                user = if (state is UpdateUserInfoState.Success) state.user.user else null,
                low = false,
                size = 50.dp,
            )

            // 昵称和ID
            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
            ) {
                when (state) {
                    null -> {
                        Text(
                            text = "请先登录awa",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    is UpdateUserInfoState.Fail -> {
                        Text(
                            text = "获取失败awa",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    is UpdateUserInfoState.Success -> {
                        Text(
                            text = state.user.user.nickname,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "BIT101 UID: ${state.user.user.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                        )
                    }

                    is UpdateUserInfoState.Loading -> {
                        Text(
                            text = "加载中awa...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {

            // 用户基本信息展示
            UserInfoShow(mainController, userInfoState)

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