package cn.bit101.android.features.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.bit101.android.data.repo.base.CommunityState
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.nav.NavDest

@Composable
internal fun SchoolContent(controller: MainController, configured: Boolean, content: @Composable () -> Unit) {
    if (configured) content() else SessionPrompt {
        Text("登录校园账号后使用课表和校园查询")
        Button(onClick = { controller.navigate(NavDest.Login()) { launchSingleTop = true } }) { Text("登录校园账号") }
    }
}

@Composable
internal fun CommunityContent(controller: MainController, state: CommunityState, onRetry: () -> Unit, content: @Composable () -> Unit) {
    if (state.connected) content() else SessionPrompt {
        Text(state.message)
        Button(onClick = onRetry, enabled = !state.connecting) { Text(if (state.connecting) "正在连接" else "立即重试") }
        TextButton(onClick = { controller.navigate(NavDest.Login(community = true)) { launchSingleTop = true } }) { Text("手动登录 BIT101") }
    }
}

@Composable
private fun SessionPrompt(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally, content = content)
}
