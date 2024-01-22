package cn.bit101.android.features.message

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.MainController
import cn.bit101.android.features.web.WebScreen
import cn.bit101.android.features.common.MessageUrl

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