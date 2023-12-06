package cn.bit101.android.ui.setting.page

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.App
import cn.bit101.android.BuildConfig
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.component.setting.SettingItemData
import cn.bit101.android.ui.component.setting.itemsGroup
import cn.bit101.android.ui.setting.viewmodel.AboutViewModel
import cn.bit101.api.model.http.app.GetVersionDataModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@Composable
fun AboutPageContent(
    paddingValues: PaddingValues,

    autoDetectUpgrade: Boolean,

    isDetectingUpgrade: Boolean,

    onDetectUpgrade: () -> Unit,
    onChangeAutoDetectUpgrade: (Boolean) -> Unit,

    onOpenAboutDialog: () -> Unit,
    onOpenLicenseDialog: () -> Unit,
) {
    val context = LocalContext.current

    val logo = App.context.applicationInfo.loadIcon(App.context.packageManager)

    val gitHubUrl = "https://github.com/flwfdd/BIT101-Android"
    val qqUrl = "https://jq.qq.com/?_wv=1027&k=OTttwrzb"
    val emailUrl = "mailto:bit101@qq.com"

    val painter = rememberDrawablePainter(logo)

    val versionItems = listOf(
        SettingItemData(
            title = "当前版本",
            onClick = onDetectUpgrade,
            enable = !isDetectingUpgrade,
            suffix = {
                Text(
                    text = BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        ),
        SettingItemData(
            title = "自动检查更新",
            subTitle = "在启动时自动检查更新",
            onClick = { onChangeAutoDetectUpgrade(!autoDetectUpgrade) },
            suffix = {
                Switch(checked = autoDetectUpgrade, onCheckedChange = onChangeAutoDetectUpgrade)
            }
        )
    )

    val contactItems = listOf(
        SettingItemData(
            title = "GitHub",
            suffix = {
                Text(
                    text = "BIT101-Android",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gitHubUrl))
                context.startActivity(intent)
            }
        ),
        SettingItemData(
            title = "QQ群",
            suffix = {
                Text(
                    text = "726965926",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qqUrl))
                context.startActivity(intent)
            }
        ),
        SettingItemData(
            title = "邮箱",
            suffix = {
                Text(
                    text = "bit101@qq.com",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            },
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(emailUrl))
                context.startActivity(intent)
            }
        ),
    )

    val aboutAppItems = listOf(
        SettingItemData(
            title = "开源声明",
            onClick = onOpenLicenseDialog,
        ),
        SettingItemData(
            title = "关于BIT101-Android",
            onClick = onOpenAboutDialog,
        ),
    )

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        item("logo") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Image(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painter,
                    contentDescription = "logo",
                )
                Spacer(modifier = Modifier.padding(2.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "BIT101",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
            Spacer(modifier = Modifier.padding(16.dp))
        }

        itemsGroup(
            title = "版本信息",
            items = versionItems,
        )

        itemsGroup(
            title = "联系我们",
            items = contactItems,
        )

        itemsGroup(
            title = "关于本APP",
            items = aboutAppItems,
        )

        item("footer") {
            Column(
                modifier = Modifier
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
}



@Composable
fun LicenseDialog(
    licenses: String,
    onClose: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        onDismissRequest = onClose,
        title = {
            Text("吃水不忘挖井人")
        },
        text = {
            val scrollState = rememberScrollState()
            SelectionContainer {
                Text(
                    text = licenses,
                    modifier = Modifier.verticalScroll(scrollState)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onClose
            ) {
                Text("关闭")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

// 关于
@Composable
fun AboutDialog(onClose: () -> Unit) {
    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = onClose,
        title = {
            Text("关于 BIT101-Android")
        },
        text = {
            val linkMap = mapOf(
                "726965926" to "https://jq.qq.com/?_wv=1027&k=OTttwrzb",
                "GitHub" to "https://github.com/flwfdd/BIT101-Android",
                "bit101@qq.com" to "mailto:bit101@qq.com",
            )
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(
                        "好耶🥳\n" +
                                "BIT101-Android 终于破壳而出啦！！\n\n" +
                                "本来私以为什么东西都要搞一个APP抑或是小程序实在不是什么好文明——明明网页就可以解决的事情为什么要那么麻烦呢？不过最后我还是想到了一些理由：比如课程表，用网页看就是不够方便；比如密码管理，在APP里确实安全很多……而做这个APP的直接契机，其实是我选修了大二下学期金老师的Android课（顺便赚点学分了属于是hh\n\n" +
                                "不论怎样，非常感谢你能来用我的APP，哦不对，不能说是我的，BIT101应该是大家的捏 _(:з」∠)_ 现在设计和功能上仍然有很多不足，不论你有功能建议还是设计灵感，或者是发现了一些BUG，又或者其实也没什么事只是路过，都欢迎加入我们的QQ交流群 "
                    )
                }

                pushStringAnnotation(tag = "726965926", annotation = linkMap["726965926"]!!)
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("726965926")
                }
                pop()

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(" 、在 ")
                }

                pushStringAnnotation(tag = "GitHub", annotation = linkMap["GitHub"]!!)
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("GitHub")
                }
                pop()

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(" 提交 issue 或邮件联系 ")
                }

                pushStringAnnotation(tag = "bit101@qq.com", annotation = linkMap["bit101@qq.com"]!!)
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("bit101@qq.com")
                }
                pop()

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(" ，BIT101期待你的贡献～\n\n最后，感谢金老师开设的高质量课程，感谢北京理工大学网络开拓者协会，感谢Shen学长在此之前制作的BIT101安卓APP，感谢帮忙测试捉虫的同学们，更要感谢每一个正在使用BIT101的你。")
                }

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append("\n\n\n\n\n")
                    repeat(42) {
                        append("BIT101\n")
                        append("BIT10 1\n")
                        append("BIT1 0 1\n")
                        append("BIT 1 0 1\n")
                        append("BI T 1 0 1\n")
                        append("B I T 1 0 1\n")
                        append("BI T 1 0 1\n")
                        append("BIT 1 0 1\n")
                        append("BIT1 0 1\n")
                        append("BIT10 1\n")
                    }
                    append("BIT101\n")
                    append("IT101\n")
                    append("T101\n")
                    append("101\n")
                    append("1\n")
                    append("\n\n\n")
                    append("这里什么也没有哦╮(￣▽￣)╭\n但既然你都辛辛苦苦翻到这里了，就送你一束花吧💐")
                }
            }

            val context = LocalContext.current
            val scrollState = rememberScrollState()
            ClickableText(
                modifier = Modifier.verticalScroll(scrollState),
                text = annotatedString,
                style=MaterialTheme.typography.bodyLarge,
                onClick = { offset ->
                    linkMap.forEach { (k, v) ->
                        annotatedString.getStringAnnotations(
                            tag = k,
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(v))
                                context.startActivity(intent)
                            }
                    }

                })
        },
        confirmButton = {
            TextButton(
                onClick = onClose
            ) {
                Text("关闭")
            }
        }
    )
}


// 更新提醒
@Composable
fun UpdateDialog(
    version: GetVersionDataModel.Response,
    onClose: () -> Unit,
    onIgnore: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Column {
                Text("海日生残夜")
                Text(
                    text = "当前版本：${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "最新版本：${version.versionName}",
                    style = MaterialTheme.typography.labelMedium
                )
            }

        },
        text = {
            val scrollState = rememberScrollState()
            Text(
                text = version.msg,
                modifier = Modifier.verticalScroll(scrollState)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onClose()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.url))
                    context.startActivity(intent)
                }
            ) {
                Text("前往下载")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClose()
                    onIgnore()
                }
            ) {
                Text("忽略该版本")
            }
        },
    )
}


@Composable
fun AboutPage(
    mainController: MainController,
    paddingValues: PaddingValues,

    vm: AboutViewModel = hiltViewModel(),
) {

    val autoDetectUpgrade by SettingDataStore.settingAutoDetectUpgrade.flow.collectAsState(initial = true)

    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showLicensesDialog by rememberSaveable { mutableStateOf(false) }
    var showUpgradeDialog by rememberSaveable { mutableStateOf(false) }

    var licenses by rememberSaveable { mutableStateOf("") }

    val checkUpdateState by vm.checkUpdateStateLiveData.observeAsState()

    LaunchedEffect(Unit) {
        MainScope().launch(Dispatchers.IO) {
            val input = App.context.assets.open("open_source_licenses.txt")
            val buffer = ByteArray(input.available())
            input.read(buffer)
            input.close()
            licenses = String(buffer)
        }
    }

    LaunchedEffect(checkUpdateState) {
        if(checkUpdateState is SimpleDataState.Fail) {
            mainController.snackbar("检查更新失败")
        } else if(checkUpdateState is SimpleDataState.Success) {
            val need = (checkUpdateState as SimpleDataState.Success).data.first
            if(need) {
                showUpgradeDialog = true
                mainController.snackbar("检测到新版本")
            } else {
                mainController.snackbar("不需要更新哦")
            }
        }
    }

    AboutPageContent(
        paddingValues = paddingValues,

        autoDetectUpgrade = autoDetectUpgrade,
        isDetectingUpgrade = checkUpdateState is SimpleDataState.Loading,

        onChangeAutoDetectUpgrade = { MainScope().launch { SettingDataStore.settingAutoDetectUpgrade.set(it) } },
        onDetectUpgrade = vm::checkUpdate,
        onOpenAboutDialog = { showAboutDialog = true },
        onOpenLicenseDialog = { showLicensesDialog = true },
    )

    if(showAboutDialog) {
        AboutDialog(
            onClose = { showAboutDialog = false }
        )
    }

    if(showLicensesDialog) {
        LicenseDialog(
            licenses = licenses,
            onClose = { showLicensesDialog = false }
        )
    }

    if(showUpgradeDialog) {
        if(checkUpdateState is SimpleDataState.Success) {
            val need = (checkUpdateState as SimpleDataState.Success).data.first
            val version = (checkUpdateState as SimpleDataState.Success).data.second
            if(need) {
                UpdateDialog(
                    version = version,
                    onClose = { showUpgradeDialog = false },
                    onIgnore = { vm.setIgnoreVersion(version.versionCode.toLong()) }
                )
            }
        }
    }
}