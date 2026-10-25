package cn.bit101.android.features.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.helper.SimpleErrorDataState

// 主界面 根据登陆状态显示登陆界面或者退出登陆界面
@Composable
fun LoginOrLogoutScreen(
    mainController: MainController,
) {
    val vm: LoginOrLogoutViewModel = hiltViewModel()

    // 检查登录状态
    val checkLoginStatusState by vm.checkLoginState.collectAsState()
    val postLoginState by vm.loginState.collectAsState()

    val sid by vm.sidFlow.collectAsState("")

    LaunchedEffect(vm) { vm.checkLoginState() }

    when {
        checkLoginStatusState is SimpleErrorDataState.Fail ||
            (checkLoginStatusState as? SimpleErrorDataState.Success)?.data == false -> {
            LoginPage(
                loginState = postLoginState,
                onLogin = vm::login,
                onLoginSuccess = {
                    mainController.popBackStack()
                    mainController.snackbar("登录成功OvO")
                },
                onLoginFailed = { mainController.snackbar(it) },
            )
        }
        (checkLoginStatusState as? SimpleErrorDataState.Success)?.data == true -> {
            LogoutPage(sid = sid, onLogout = vm::logout, onBack = mainController::popBackStack)
        }
        else -> {
            Column(
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center).width(64.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
