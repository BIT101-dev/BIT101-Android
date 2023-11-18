package cn.bit101.android.ui

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration

class MainController(
    val scope: CoroutineScope,
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState
) {
    fun snackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message,
                withDismissAction = true,
            )
        }
    }

    fun copyText(cm: ClipboardManager, text: AnnotatedString) {
        Log.i("copyText", text.text)
        cm.setText(text)
        scope.launch {
            snackbarHostState.showSnackbar(
                "已复制",
                duration = SnackbarDuration.Short,
                withDismissAction = true,
            )
        }
    }
}