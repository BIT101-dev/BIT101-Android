package cn.bit101.android.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

// 退出登陆界面
@Composable
fun LogoutPage(
    sid: String?,

    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val underlineColor = MaterialTheme.colorScheme.secondaryContainer
        Text(
            "已经登陆",
            style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.drawBehind {
                val strokeWidth = 10.dp.toPx()
                val verticalCenter = size.height - 0.75f * strokeWidth
                drawLine(
                    color = underlineColor,
                    strokeWidth = strokeWidth,
                    start = Offset(-5.dp.toPx(), verticalCenter),
                    end = Offset(size.width + 5.dp.toPx(), verticalCenter),
                    cap = StrokeCap.Round
                )
            },
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = sid ?: "无法获取sid",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onBack,
        ) {
            Text(text = "返回上级")
        }
        Button(
            onClick = {
                onLogout()
                onBack()
            },
        ) {
            Text(text = "退出登录")
        }
    }
}

