package cn.bit101.android.ui.component.bottomsheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.component.bottomsheet.CoreBottomSheetDragHandle

/**
 * A bottom sheet drag handle will be displayed as a rounded rectangle.
 */
@Composable
fun BottomSheetDragHandle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = 0.4f
    ),
    height: Dp = 24.dp,
    barWidth: Dp = 32.dp,
    barHeight: Dp = 4.dp,
) {
    CoreBottomSheetDragHandle(modifier, color, height, barWidth, barHeight)
}