package cn.bit101.android.ui.component.snackbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable

@Composable
fun SnackbarHost(
    state: SnackbarState,
) {
    if(state.message != null) {
        Snackbar(
            message = state.message!!,
            onDismiss = { state.dismiss() },
        )
    }
}