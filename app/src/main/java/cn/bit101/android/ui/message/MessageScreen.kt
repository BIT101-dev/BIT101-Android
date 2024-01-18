package cn.bit101.android.ui.message

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.MessageUrl
import cn.bit101.android.ui.web.WebScreen

@Composable
fun MessageScreen(
    mainController: MainController,
    vm: MessageViewModel = hiltViewModel(),
) {
    WebScreen(
        mainController = mainController,
        url = MessageUrl,
    )
}