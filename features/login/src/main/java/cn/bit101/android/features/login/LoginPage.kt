package cn.bit101.android.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.helper.SimpleState

// 登录界面
@Composable
internal fun LoginPage(
    loginState: SimpleState?,

    onLogin: (String, String) -> Unit,
    onLoginSuccess: () -> Unit = {},
    onLoginFailed: () -> Unit = {},
) {
    var loading by remember { mutableStateOf(false) }

    // 当登录状态改变时触发
    // 这里控制的是消息的显示和页面的跳转
    LaunchedEffect(loginState) {
        when(loginState) {
            // 登录成功，显示消息，并且退回上一个页面，同时获取用户的信息
            is SimpleState.Success -> {
                onLoginSuccess()
                loading = false
            }

            // 登录失败，显示消息
            SimpleState.Fail -> {
                onLoginFailed()
                loading = false
            }

            // 登录中，应该让按钮转圈圈
            SimpleState.Loading -> {
                loading = true
            }

            // 空，停止转圈
            null -> {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var sid by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var passwordHidden by rememberSaveable { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .padding(horizontal = 42.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val underlineColor = MaterialTheme.colorScheme.secondaryContainer
            Text(
                "登录BIT101",
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
            OutlinedTextField(
                value = sid,
                onValueChange = { sid = it },
                label = { Text("学号") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "StudentIdIcon",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "PasswordIcon",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = { passwordHidden = !passwordHidden }) {
                        val visibilityIcon =
                            if (passwordHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        val description = if (passwordHidden) "显示密码" else "隐藏密码"
                        Icon(imageVector = visibilityIcon, contentDescription = description)
                    }
                }
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
                onClick = {
                    onLogin(sid, password)
                },
            ) {
                if (loading) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                else Text(text = "登录")
            }
            Text(
                text = "Tips：使用学校统一身份认证账号密码进行登录，若未注册过BIT101账号将会自动注册。BIT101尊重您的隐私，密码只会经不可逆加密后传输到服务器。",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}