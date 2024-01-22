package cn.bit101.android.ui.versions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import cn.bit101.android.BuildConfig
import cn.bit101.android.config.user.base.LoginStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun VersionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    vm: VersionDialogViewModel = hiltViewModel()
) {
    val version = BuildConfig.VERSION_CODE

    if(version == 4) {
        val status by vm.statusFlow.collectAsState(initial = null)

        if(status == null) return
        else if(status == false) {
            onDismiss()
            return
        }

        Version4Dialog(
            onConfirm = {
                onConfirm()
                onDismiss()
            }
        )
    }
}

@HiltViewModel
class VersionDialogViewModel @Inject constructor(
    private val loginStatus: LoginStatus
) : ViewModel() {
    val statusFlow = loginStatus.status.flow
}