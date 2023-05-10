package cn.bit101.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import cn.bit101.android.MainController
import cn.bit101.android.net.HttpClient
import cn.bit101.android.net.school.checkLogin
import cn.bit101.android.net.school.login
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 2023/3/18 0:07
 * @description _(:з」∠)_
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(mainController: MainController, changeLoginStatus: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var sid by remember { mutableStateOf(TextFieldValue("")) }
        var password by remember { mutableStateOf(TextFieldValue("")) }
        var passwordHidden by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .padding(horizontal = 42.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val underlineColor = MaterialTheme.colorScheme.secondaryContainer
            var loading by remember { mutableStateOf(false) }
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
                    MainScope().launch {
                        loading = true
                        if (login(sid.text, password.text)) {
                            loading = false
                            changeLoginStatus(true)
                            mainController.navController.popBackStack()
                            mainController.snackbar("登录成功OvO")
                        } else {
                            loading = false
                            changeLoginStatus(false)
                            mainController.snackbar("登录失败Orz")
                        }
                    }
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


@Composable
fun Logout(changeLoginStatus: (Boolean) -> Unit) {
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
        Button(
            onClick = {
                HttpClient.cookieManager.cookieStore.removeAll()
                changeLoginStatus(false)
            },
        ) {
            Text(text = "退出登录")
        }
    }
}


@Composable
fun LoginOrLogout(mainController: MainController) {
    // TODO:使用全局状态检查是否登录
    var isLogin by remember { mutableStateOf(false) }
    LaunchedEffect(true) { isLogin = checkLogin() }
    if (isLogin) Logout { status:Boolean -> isLogin = status }
    else Login(mainController) { status:Boolean -> isLogin = status }
}
