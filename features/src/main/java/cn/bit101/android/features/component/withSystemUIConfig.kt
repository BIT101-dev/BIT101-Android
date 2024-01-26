package cn.bit101.android.features.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import cn.bit101.android.features.common.utils.ColorUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController

internal data class SystemUIConfig(
    val statusBarColor: Color,
    val statusBarDarkIcon: Boolean,
    val navBarColor: Color,
)

@Composable
internal fun WithSystemUIConfig(
    systemUIConfig: SystemUIConfig,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(systemUIConfig) {
        systemUiController.setStatusBarColor(
            color = systemUIConfig.statusBarColor,
            darkIcons = systemUIConfig.statusBarDarkIcon
        )
        systemUiController.setNavigationBarColor(
            color = systemUIConfig.navBarColor
        )
    }

    content()

    val darkIcon = ColorUtils.isLightColor(MaterialTheme.colorScheme.background)

    DisposableEffect(Unit) {
        onDispose {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = darkIcon
            )
            systemUiController.setNavigationBarColor(
                color = Color.Transparent
            )
        }
    }
}