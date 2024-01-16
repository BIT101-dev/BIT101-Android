package cn.bit101.android.ui.setting.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.setting.SettingItemData
import cn.bit101.android.ui.component.setting.SettingsColumn
import cn.bit101.android.ui.component.setting.SettingsGroup
import cn.bit101.android.ui.setting.viewmodel.AccountViewModel
import cn.bit101.api.model.common.User


@Composable
private fun AccountPageContent(
    ifLogin: Boolean,
    checkingLogin: Boolean,
    user: User?,
    sid: String?,
    onCheckLogin: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
) {
    val infoItems = listOf(
        SettingItemData.Custom(
            title = "头像",
            onClick = {},
            suffix = {
                Avatar(
                    user = user,
                    low = false,
                    showIdentity = false,
                )
            }
        ),
        SettingItemData.Button(
            title = "昵称",
            text = user?.nickname ?: "",
        ),
        SettingItemData.Button(
            title = "个性签名",
            text = user?.motto ?: "",
        ),
        SettingItemData.Card(
            title = "学号",
            text = sid ?: "",
        ),
        SettingItemData.Card(
            title = "UID",
            text = user?.id?.toString() ?: "",
        ),
    )

    val loginItems = listOf(
        SettingItemData.Button(
            title = "登录状态检查",
            subTitle = "登录分为两步，首先进行学校统一身份认证，然后登录BIT101。点击检查二者登录状态",
            onClick = onCheckLogin,
            enable = !checkingLogin,
            text = if (ifLogin) "已登录" else "未登录",
        ),

        if(ifLogin) SettingItemData.Button(
            title = "退出登录",
            onClick = onLogout,
        ) else SettingItemData.Button(
            title = "登录",
            onClick = onLogin,
        ),
    )

    SettingsColumn{
        SettingsGroup(
            title = "个人信息",
            items = infoItems,
            visible = ifLogin && user != null,
        )
        SettingsGroup(
            title = "登录状态",
            items = loginItems,
        )
    }
}

@Composable
fun AccountPage(
    mainController: MainController,
    onLogin: () -> Unit,
    vm: AccountViewModel = hiltViewModel()
) {
    val getUserInfoState by vm.getUserInfoStateFlow.collectAsState()

    val ifLogin by vm.loginStatusFlow.collectAsState(initial = false)

    val checkingLoginState by vm.checkLoginStateFlow.collectAsState()

    val sid by vm.sidFlow.collectAsState(initial = null)

    LaunchedEffect(ifLogin) {
        if (ifLogin && (getUserInfoState == null || getUserInfoState is SimpleDataState.Fail)) {
            vm.getUserInfo()
        }
    }

    LaunchedEffect(checkingLoginState) {
        if (checkingLoginState is SimpleState.Fail) {
            mainController.snackbar("登录状态检查失败")
        } else if (checkingLoginState is SimpleState.Success) {
            mainController.snackbar("登录状态检查成功")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.clearStates()
        }
    }


    val user = (getUserInfoState as? SimpleDataState.Success)?.data?.user

    AccountPageContent(
        user = user,
        sid = sid,
        ifLogin = ifLogin,
        checkingLogin = checkingLoginState == SimpleState.Loading,
        onCheckLogin = vm::checkLogin,
        onLogout = vm::logout,
        onLogin = onLogin
    )

}