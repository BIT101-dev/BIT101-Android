package cn.bit101.android.features.common.component.snackbar

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