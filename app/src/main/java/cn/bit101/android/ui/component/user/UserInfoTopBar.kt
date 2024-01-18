package cn.bit101.android.ui.component.user

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.component.topbar.BasicTwoRowsTopAppBar

@Composable
private fun ColorScheme.applyTonalElevation(backgroundColor: Color, elevation: Dp): Color {
    val tonalElevationEnabled = LocalTonalElevationEnabled.current
    return if (backgroundColor == surface && tonalElevationEnabled) {
        surfaceColorAtElevation(elevation)
    } else {
        backgroundColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable BoxScope.() -> Unit,
    smallTitle: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.applyTonalElevation(
            backgroundColor = MaterialTheme.colorScheme.surface,
            elevation = 3.0.dp
        ),
    ),
    pinnedHeight: Dp = 64.0.dp,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    BasicTwoRowsTopAppBar(
        modifier = modifier,
        title = title,
        smallTitle = smallTitle,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        pinnedHeight = pinnedHeight,
        scrollBehavior = scrollBehavior
    )
}