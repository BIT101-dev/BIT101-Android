package cn.bit101.android.features.message

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.MainController

@Composable
fun MessageScreen(mainController: MainController) {
    val vm: MessageViewModel = hiltViewModel()
}