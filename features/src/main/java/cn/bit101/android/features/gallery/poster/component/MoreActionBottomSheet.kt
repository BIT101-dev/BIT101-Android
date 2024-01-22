package cn.bit101.android.features.gallery.poster.component

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.utils.ColorUtils
import cn.bit101.android.features.component.bottomsheet.BottomSheet
import cn.bit101.android.features.component.bottomsheet.BottomSheetDefaults
import cn.bit101.android.features.component.bottomsheet.BottomSheetState
import cn.bit101.android.features.component.bottomsheet.DialogSheetBehaviors

@Composable
internal fun MoreActionBottomSheetAction(
    icon: ImageVector,
    text: String,

    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
            .padding(6.dp)
            .width(64.dp),
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

data class Action(
    val icon: ImageVector,
    val text: String,
    val onClick: () -> Unit,
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreActionBottomSheet(
    state: BottomSheetState,
    actions: List<Action>,

    onDismiss: () -> Unit,
) {
    BottomSheet(
        state = state,
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
                .padding(horizontal = 8.dp, vertical = 24.dp)
                .fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier.align(Alignment.Center),
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