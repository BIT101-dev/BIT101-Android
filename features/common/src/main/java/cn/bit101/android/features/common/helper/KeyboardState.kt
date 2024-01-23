package cn.bit101.android.features.common.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
fun keyboardStateAsState(): Pair<State<Dp>, State<Boolean>> {
    val density = LocalDensity.current
    val keyboardHeight = remember { mutableStateOf(0.dp) }
    val keyboardVisible = remember { mutableStateOf(false) }
    val view = LocalView.current
    LaunchedEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            keyboardHeight.value = density.run {
                insets.getInsets(WindowInsetsCompat.Type.ime()).bottom.toDp()
            }
            keyboardVisible.value = insets.isVisible(WindowInsetsCompat.Type.ime())
            insets
        }
    }
    return Pair(keyboardHeight, keyboardVisible)
}