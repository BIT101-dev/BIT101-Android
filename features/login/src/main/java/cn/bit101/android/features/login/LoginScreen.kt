package cn.bit101.android.features.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.successMessage

@Composable
fun LoginScreen(mainController: MainController, initialCommunity: Boolean = false) {
    val vm: LoginViewModel = hiltViewModel()
    val sid by vm.sid.collectAsState(initial = null)
    val state by vm.loginState.collectAsState()
    val schoolResult by vm.schoolResult.collectAsState()
    if (sid == null) return
    var communityOnly by rememberSaveable { mutableStateOf(initialCommunity) }
    var schoolIdentity by rememberSaveable { mutableStateOf(true) }
    val loading = state is SimpleErrorDataState.Loading

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TabRow(selectedTabIndex = if (communityOnly) 1 else 0) {
            listOf("校园账号", "BIT101").forEachIndexed { index, title ->
                Tab(selected = communityOnly == (index == 1), enabled = !loading,
                    onClick = { vm.clearResult(); communityOnly = index == 1 }, text = { Text(title) })
            }
        }
        if (schoolResult != null && !communityOnly && loading) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("校园账号已登录", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (schoolResult?.teachingCenterWarning != null) "教务暂不可用，可稍后重试" else "正在连接 BIT101…",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = {
                        if (vm.enterCampus()) {
                            mainController.popBackStack()
                            mainController.snackbar(schoolResult?.successMessage() ?: "校园登录成功")
                        }
                    }) { Text("先进入") }
                }
            }
        }
        if (communityOnly) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                listOf("统一身份认证", "BIT101 密码").forEachIndexed { index, label ->
                    TextButton(enabled = !loading, onClick = { vm.clearResult(); schoolIdentity = index == 0 }) {
                        Text(if (schoolIdentity == (index == 0)) "✓ $label" else label)
                    }
                }
            }
        }
        key(communityOnly, schoolIdentity) {
            LoginPage(
                loginState = state,
                title = if (communityOnly) "登录 BIT101" else "登录校园账号",
                initialSid = sid.orEmpty(),
                usernameEnabled = !communityOnly || sid.isNullOrBlank(),
                description = when {
                    !communityOnly -> "使用学校账号密码。校园登录成功后尝试连接 BIT101；连接失败不影响校园功能。首次连接会自动创建 BIT101 账号。"
                    schoolIdentity -> "使用学校账号密码验证身份。新用户自动注册，已有 BIT101 密码保持不变。"
                    else -> "使用已有 BIT101 账号的独立密码登录。"
                },
                onLogin = { username, password -> vm.login(username, password, communityOnly, schoolIdentity) },
                onLoginSuccess = { message ->
                    mainController.popBackStack()
                    mainController.snackbar(message)
                },
                onLoginFailed = { mainController.snackbar(it) },
            )
        }
    }
}
