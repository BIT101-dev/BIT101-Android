package cn.bit101.android.ui

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainController(
    val scope: CoroutineScope,
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState
) {
    fun snackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
}