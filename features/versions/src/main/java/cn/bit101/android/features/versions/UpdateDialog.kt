package cn.bit101.android.features.versions

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.AboutSettings
import cn.bit101.android.data.repo.base.VersionRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.getAppVersion
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleDataStateFlow
import cn.bit101.api.model.http.app.GetVersionDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@Composable
fun UpdateDialog(
    version: GetVersionDataModel.Response,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit,
) {
    val context = LocalContext.current
    val appVersion = getAppVersion(context)
    AlertDialog(
        onDismissRequest = {
            if(appVersion.versionNumber >= version.minVersionCode) {
                onDismiss()
            }
        },
        title = {
            Column {
                Text("海日生残夜")
                Text(
                    text = "当前版本：${appVersion.versionNumber}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "最新版本：${version.versionName}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "最低版本：${version.minVersionName}",
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
                    if (appVersion.versionNumber < version.minVersionCode) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.url))
                        context.startActivity(intent)
                    } else {
                        onIgnore()
                        onDismiss()
                    }

                }
            ) {
                if (appVersion.versionNumber < version.minVersionCode) {
                    Text("强制更新")
                } else {
                    Text("忽略该版本")
                }
            }
        },
    )
}


@Composable
fun UpdateDialog() {
    val vm: UpdateDialogViewModel = hiltViewModel()

    val versionState by vm.getVersionStateFlow.collectAsState()
    val context = LocalContext.current
    val appVersion = getAppVersion(context)

    LaunchedEffect(Unit) {
        if(versionState == null) {
            vm.getVersion()
        }
    }

    val version = (versionState as? SimpleDataState.Success)?.data ?: return
    val ignoreVersion by vm.ignoreVersionFlow.collectAsState(initial = null)
    if(ignoreVersion == null) return

    var show by rememberSaveable { mutableStateOf(false) }

    if((show && version.versionCode > ignoreVersion!!) || appVersion.versionNumber < version.minVersionCode) {
        UpdateDialog(
            version = version,
            onDismiss = { show = false },
            onIgnore = { vm.ignore(version) }
        )
    }
}

@HiltViewModel
internal class UpdateDialogViewModel @Inject constructor(
    private val aboutSettings: AboutSettings,
    private val versionRepo: VersionRepo
) : ViewModel() {
    private val _getVersionStateFlow = MutableStateFlow<SimpleDataState<GetVersionDataModel.Response>?>(null)
    val getVersionStateFlow = _getVersionStateFlow.asStateFlow()

    val ignoreVersionFlow = aboutSettings.ignoredVersion.flow


    fun getVersion() = withSimpleDataStateFlow(_getVersionStateFlow) {
        versionRepo.getVersionInfo()
    }

    fun ignore(version: GetVersionDataModel.Response) = withScope {
        aboutSettings.ignoredVersion.set(version.versionCode.toLong())
    }
}