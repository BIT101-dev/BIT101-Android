package cn.bit101.android.features.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.nav.NavDest

@Composable
internal fun WithLoginStatus(
    mainController: MainController,
    status: Boolean?,
    content: @Composable () -> Unit
) {
    when (status) {
        null -> {
            // 未知状态
        }
        true -> {
            content()
        }
        false -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    mainController.navigate(NavDest.Login) {
                        launchSingleTop = true
                    }
                }) {
                    Text("登录")
                }
            }
        }
    }
}