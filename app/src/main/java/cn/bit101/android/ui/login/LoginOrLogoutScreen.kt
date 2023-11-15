package cn.bit101.android.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController

// 主界面 根据登陆状态显示登陆界面或者退出登陆界面
@Composable
fun LoginOrLogoutScreen(
    mainController: MainController,
    vm: LoginOrLogoutViewModel = hiltViewModel(),
) {
    // 检查登录状态
    val checkLoginStatusState by vm.checkLoginStateLiveData.observeAsState()
    val postLoginState by vm.postLoginStateLiveData.observeAsState()

    val sid by vm.sidFlow.collectAsState("")


    // 根据检查登录的情况显示不同的内容
    when (checkLoginStatusState) {
        // 检查失败，显示登录页面
        is CheckLoginStateState.Fail -> {
            LoginPage(
                loginState = postLoginState,
                onLogin = vm::login,
                onLoginSuccess = {
                    mainController.navController.popBackStack()
                    mainController.snackbar("登录成功OvO")
                },
                onLoginFailed = {
                    mainController.snackbar("登录失败Orz")
                }
            )
        }

        // 检查成功，显示登出页面
        is CheckLoginStateState.Success -> {
            LogoutPage(
                sid = sid,
                onLogout = vm::logout,
                onBack = mainController.navController::popBackStack
            )
        }

        // 检查中，显示加载动画
        is CheckLoginStateState.Checking -> {
            // 这里应该有一个加载动画
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .width(64.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 如果还没有检查过，那么就进行检查
        null -> {
            vm.checkLoginState()
        }
    }
}
