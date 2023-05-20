package cn.bit101.android.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.App
import cn.bit101.android.BuildConfig
import cn.bit101.android.MainController
import cn.bit101.android.R
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.viewmodel.SettingViewModel
import coil.compose.AsyncImage
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 2023/5/18 下午2:24
 * @description 配置页面
 * _(:з」∠)_
 */

@Composable
fun Setting(mainController: MainController, vm: SettingViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {

        // 用户基本信息展示
        Row(
            Modifier
                .padding(10.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 默认头像为APP图标
            val icon = App.context.applicationInfo.loadIcon(App.context.packageManager)
            val painter = rememberDrawablePainter(icon)
            var loaded by remember { mutableStateOf(false) }
            AsyncImage(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(App.context.getColor(R.color.ic_launcher_background))),
                contentScale= ContentScale.FillBounds,
                model = vm.userInfo.value?.avatar,
                placeholder = painter,
                error = painter,
                fallback = painter,
                onSuccess = {
                    loaded = true
                },
                contentDescription = "avatar"
            )

            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
            ) {
                if (vm.userInfo.value == null) {
                    Text(
                        text = "请先登录awa",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        text = vm.userInfo.value!!.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "BIT101 UID: ${vm.userInfo.value!!.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }


        val showLicenseDialog = remember { mutableStateOf(false) }
        val showUpdateDialog = remember { mutableStateOf(false) }

        // 首次开屏显示更新提醒
        val showedUpdateDialog = rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (!showedUpdateDialog.value &&
                vm.checkUpdate() &&
                vm.versionInfo!!.version_code.toLong() != vm.ignoreVersionFlow.first()
            ) {
                showUpdateDialog.value = true
                showedUpdateDialog.value = true
            }
        }

        // 设置选项
        ConfigColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            scrollable = false,
            items = listOf(
                ConfigItem.Button(
                    title = "登陆管理",
                    onClick = {
                        mainController.navController.navigate("login") {
                            launchSingleTop = true
                        }
                    }
                ),
                ConfigItem.Switch(
                    title = "自动旋转",
                    checked = vm.rotateFlow.collectAsState(initial = false).value,
                    onCheckedChange = {
                        MainScope().launch {
                            vm.setRotate(it)
                        }
                    }
                ),
                ConfigItem.Switch(
                    title = "动态适配系统主题",
                    checked = vm.dynamicThemeFlow.collectAsState(initial = false).value,
                    onCheckedChange = {
                        MainScope().launch {
                            vm.setDynamicTheme(it)
                        }
                    }
                ),
                ConfigItem.Switch(
                    title = "禁用暗黑模式",
                    checked = vm.disableDarkThemeFlow.collectAsState(initial = false).value,
                    onCheckedChange = {
                        MainScope().launch {
                            vm.setDisableDarkTheme(it)
                        }
                    }
                ),
                ConfigItem.Button(
                    title = "检查更新",
                    content = "当前版本：${BuildConfig.VERSION_NAME}",
                    onClick = {
                        MainScope().launch {
                            if (vm.checkUpdate()) {
                                showUpdateDialog.value = true
                            } else {
                                mainController.snackbar("没有检测到新版本awa")
                            }
                        }
                    }
                ),
                ConfigItem.Button(
                    title = "查看开源声明",
                    onClick = {
                        showLicenseDialog.value = true
                    }
                ),
            ))

        if (showLicenseDialog.value) {
            LicenseDialog(vm, showLicenseDialog)
        }
        if (showUpdateDialog.value) {
            UpdateDialog(vm, showUpdateDialog)
        }

        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val githubUrl = "https://github.com/flwfdd/BIT101-Android"
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append("欢迎到 ")
                }

                pushStringAnnotation(tag = "github", annotation = githubUrl)
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("GitHub")
                }
                pop()

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append(" 给上一颗可爱的\uD83C\uDF1F")
                }
            }

            val context = LocalContext.current
            ClickableText(text = annotatedString, onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "github", start = offset, end = offset)
                    .firstOrNull()?.let {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(intent)
                    }
            })

            Text(text = "Powered⚡ by fdd with 💖.")
        }
    }
}

// 开源声明
@Composable
fun LicenseDialog(vm: SettingViewModel, showDialog: MutableState<Boolean>) {
    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Text("吃水不忘挖井人")
        },
        text = {
            val scrollState = rememberScrollState()
            SelectionContainer {
                Text(text = vm.getLicenses(), modifier = Modifier.verticalScroll(scrollState))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                }
            ) {
                Text("关闭")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

// 更新提醒
@Composable
fun UpdateDialog(vm: SettingViewModel, showDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Column {
                Text("海日生残夜")
                Text(
                    text = "当前版本：${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "最新版本：${vm.versionInfo?.version_name ?: ""}",
                    style = MaterialTheme.typography.labelMedium
                )
            }

        },
        text = {
            val scrollState = rememberScrollState()
            Text(
                text = vm.versionInfo?.msg ?: "",
                modifier = Modifier.verticalScroll(scrollState)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(vm.versionInfo!!.url))
                    context.startActivity(intent)
                }
            ) {
                Text("前往下载")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                    vm.ignoreUpdate()
                }
            ) {
                Text("忽略该版本")
            }
        },
    )
}
