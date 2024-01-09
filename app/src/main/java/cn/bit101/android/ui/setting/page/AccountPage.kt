package cn.bit101.android.ui.setting.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.common.CircularProgressIndicatorForPage
import cn.bit101.android.ui.component.common.ErrorMessageForPage
import cn.bit101.android.ui.component.setting.SettingItemData
import cn.bit101.android.ui.component.setting.itemsGroup
import cn.bit101.android.ui.setting.viewmodel.AccountViewModel
import cn.bit101.api.model.common.User


@Composable
private fun AccountPageContent(
    mainController: MainController,
    user: User,
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
        SettingItemData.ButtonWithSuffixText(
            title = "昵称",
            text = user.nickname,
        ),
        SettingItemData.ButtonWithSuffixText(
            title = "个性签名",
            text = user.motto,
        )
    )

    val loginItems = listOf(
        SettingItemData.Button(
            title = "学校统一身份认证",
            subTitle = "点击检查学校统一身份认证状态",
            onClick = {}
        ),
        SettingItemData.Button(
            title = "BIT101登录状态",
            subTitle = "点击检查BIT101登录状态",
            onClick = {}
        ),
        SettingItemData.Button(
            title = "退出登录",
            onClick = {}
        ),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
    ) {
        itemsGroup(
            title = "个人信息",
            items = infoItems,
        )
        itemsGroup(
            title = "登录状态",
            subTitle = "登录分为两步，首先进行学校统一身份认证，然后登录BIT101",
            items = loginItems,
        )
    }
}

@Composable
fun AccountPage(
    mainController: MainController,
    vm: AccountViewModel = hiltViewModel()
) {
    val getUserInfoState by vm.getUserInfoStateFlow.collectAsState()

    LaunchedEffect(Unit) {
        if (getUserInfoState == null) {
            vm.getUserInfo()
        }
    }

    when(getUserInfoState) {
        null -> {}

        is SimpleDataState.Loading -> {
            CircularProgressIndicatorForPage()
        }

        is SimpleDataState.Success -> {
            val user = (getUserInfoState as SimpleDataState.Success).data.user
            AccountPageContent(
                mainController = mainController,
                user = user
            )
        }

        is SimpleDataState.Fail -> {
            ErrorMessageForPage()
        }
    }

}