package cn.bit101.android.features.poster.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.runtime.Composable
import cn.bit101.android.features.common.component.bottomsheet.BottomSheetState


@Composable
fun MoreActionOfPosterBottomSheet(
    state: BottomSheetState,
    own: Boolean,

    onDelete: () -> Unit,
    onReport: () -> Unit,
    onEdit: () -> Unit,
    onOpenInBrowser: () -> Unit,

    onDismiss: () -> Unit,
) {
    val actions = if (own) {
        listOf(
            Action(
                icon = Icons.Outlined.Edit,
                text = "修改",
                onClick = onEdit,
            ),
            Action(
                icon = Icons.Outlined.Delete,
                text = "删除",
                onClick = onDelete,
            ),
            Action(
                icon = Icons.Outlined.OpenInBrowser,
                text = "外部打开",
                onClick = onOpenInBrowser,
            ),
            Action(
                icon = Icons.Outlined.Info,
                text = "举报",
                onClick = onReport,
            ),
        )
    } else {
        listOf(
            Action(
                icon = Icons.Outlined.OpenInBrowser,
                text = "外部打开",
                onClick = onOpenInBrowser,
            ),
            Action(
                icon = Icons.Outlined.Info,
                text = "举报",
                onClick = onReport,
            ),
        )
    }

    MoreActionBottomSheet(
        state = state,
        actions = actions,
        onDismiss = onDismiss,
    )
}