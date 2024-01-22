package cn.bit101.android.features.gallery.poster.component

import android.view.HapticFeedbackConstants
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import cn.bit101.android.features.component.bottomsheet.BottomSheetState

@Composable
fun MoreActionOfCommentBottomSheet(
    state: BottomSheetState,
    own: Boolean,

    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onReport: () -> Unit,

    onDismiss: () -> Unit,
) {
    val view = LocalView.current

    LaunchedEffect(state.visible) {
        if(state.visible) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    val actions = if(own) listOf(
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
    ) else {
        listOf(
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
    }

    MoreActionBottomSheet(
        state = state,
        actions = actions,
        onDismiss = onDismiss,
    )
}