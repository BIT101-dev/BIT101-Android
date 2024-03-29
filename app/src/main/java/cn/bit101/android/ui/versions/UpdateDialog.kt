package cn.bit101.android.ui.versions

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
import cn.bit101.android.BuildConfig
import cn.bit101.android.manager.base.AboutSettingManager
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.withScope
import cn.bit101.android.ui.common.withSimpleDataStateFlow
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
    AlertDialog(
        onDismissRequest = {
            if(BuildConfig.VERSION_CODE >= version.minVersionCode) {
                onDismiss()
            }
        },
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
                    if (BuildConfig.VERSION_CODE < version.minVersionCode) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.url))
                        context.startActivity(intent)
                    } else {
                        onIgnore()
                        onDismiss()
                    }

                }
            ) {
                if (BuildConfig.VERSION_CODE < version.minVersionCode) {
                    Text("强制更新")
                } else {
                    Text("忽略该版本")
                }
            }
        },
    )
}


@Composable
fun UpdateDialog(
    vm: UpdateDialogViewModel = hiltViewModel(),
) {
    val versionState by vm.getVersionStateFlow.collectAsState()

    LaunchedEffect(Unit) {
        if(versionState == null) {
            vm.getVersion()
        }
    }

    val version = (versionState as? SimpleDataState.Success)?.data ?: return
    val ignoreVersion by vm.ignoreVersionFlow.collectAsState(initial = null)
    if(ignoreVersion == null) return

    var show by rememberSaveable { mutableStateOf(false) }

    if((show && version.versionCode > ignoreVersion!!) || BuildConfig.VERSION_CODE < version.minVersionCode) {
        UpdateDialog(
            version = version,
            onDismiss = { show = false },
            onIgnore = { vm.ignore(version) }
        )
    }
}

@HiltViewModel
class UpdateDialogViewModel @Inject constructor(
    private val aboutSettingManager: AboutSettingManager,
    private val versionRepo: VersionRepo
) : ViewModel() {
    private val _getVersionStateFlow = MutableStateFlow<SimpleDataState<GetVersionDataModel.Response>?>(null)
    val getVersionStateFlow = _getVersionStateFlow.asStateFlow()

    val ignoreVersionFlow = aboutSettingManager.ignoredVersion.flow


    fun getVersion() = withSimpleDataStateFlow(_getVersionStateFlow) {
        versionRepo.getVersionInfo()
    }

    fun ignore(version: GetVersionDataModel.Response) = withScope {
        aboutSettingManager.ignoredVersion.set(version.versionCode.toLong())
    }
}