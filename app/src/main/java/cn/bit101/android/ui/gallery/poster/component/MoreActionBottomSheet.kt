package cn.bit101.android.ui.gallery.poster.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.component.bottomsheet.BottomSheet
import cn.bit101.android.ui.component.bottomsheet.BottomSheetDefaults
import cn.bit101.android.ui.component.bottomsheet.BottomSheetValue
import cn.bit101.android.ui.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.ui.component.bottomsheet.rememberBottomSheetState
import cn.bit101.android.utils.ColorUtils

@Composable
internal fun MoreActionBottomSheetAction(
    icon: ImageVector,
    text: String,

    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.padding(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
        )
    }
}

internal data class Action(
    val icon: ImageVector,
    val text: String,
    val onClick: () -> Unit,
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreActionBottomSheet(
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onReport: () -> Unit,

    onDismiss: () -> Unit,
) {
    val view = LocalView.current

    LaunchedEffect(Unit) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
    val actions = listOf(
        Action(
            icon = Icons.Outlined.Delete,
            text = "删除",
            onClick = onDelete,
        ),
        Action(
            icon = Icons.Outlined.CopyAll,
            text = "复制",
            onClick = onCopy,
        ),
        Action(
            icon = Icons.Outlined.Info,
            text = "举报",
            onClick = onReport,
        ),
    )

    BottomSheet(
        state = rememberBottomSheetState(
            initialValue = BottomSheetValue.Expanded,
            confirmValueChange = {
                if (it == BottomSheetValue.Collapsed) {
                    onDismiss()
                    false
                } else true
            },
        ),
        skipPeeked = true,
        allowNestedScroll = false,
        behaviors = DialogSheetBehaviors(
            navigationBarColor = BottomSheetDefaults.backgroundColor,
            lightNavigationBar = ColorUtils.isLightColor(BottomSheetDefaults.backgroundColor),
        ),
        dragHandle = {},
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier
                    .width(260.dp)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 8,
            ) {
                actions.forEach { action ->
                    MoreActionBottomSheetAction(
                        icon = action.icon,
                        text = action.text,
                        onClick = {
                            onDismiss()
                            action.onClick()
                        },
                    )
                }
            }
        }
    }
}