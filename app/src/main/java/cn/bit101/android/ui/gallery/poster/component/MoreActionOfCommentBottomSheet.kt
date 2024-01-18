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
import cn.bit101.android.ui.component.bottomsheet.BottomSheetState
import cn.bit101.android.ui.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.utils.ColorUtils

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