package cn.bit101.android.features.common.component.snackbar

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import cn.bit101.android.features.common.component.DialogLayout
import cn.bit101.android.features.common.component.SnackbarWrapper
import cn.bit101.android.features.common.component.bottomsheet.CoreBottomSheetDefaults
import cn.bit101.android.features.common.component.bottomsheet.DialogSheetBehaviors
import java.util.UUID
import kotlin.math.roundToInt

@Composable
private fun SnackbarContent(
    modifier: Modifier = Modifier,
    message: MessageData,
    behaviors: DialogSheetBehaviors = CoreBottomSheetDefaults.dialogSheetBehaviors(),
    onDismiss: () -> Unit,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val composition = rememberCompositionContext()

    val scope = rememberCoroutineScope()

    val layoutDirection = LocalLayoutDirection.current

    val dialogId = rememberSaveable { UUID.randomUUID() }

    val (finalModifier, finalBehaviors) = remember(modifier, behaviors) {
        if (behaviors.dialogWindowSoftInputMode ==
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
        ) {
            modifier to behaviors.copy(
                dialogWindowSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
            )
        } else {
            modifier to behaviors
        }
    }

    var showed by remember(message) { mutableStateOf(false) }

    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
    )

    // 大小的动画
    val scale = 1f

    val paddingHorizontal = 24.dp
    val paddingVertical = 12.dp

    val width = density.run { (message.message.length * textStyle.fontSize).toPx() + paddingHorizontal.toPx() * 2 }
    val height = density.run { textStyle.fontSize.toPx() + paddingVertical.toPx() * 2 }

    val dialog = remember(view, message) {
        SnackbarWrapper(
            onDismissRequest = onDismiss,
            behaviors = finalBehaviors,
            composeView = view,
            layoutDirection = layoutDirection,
            dialogId = dialogId,
            width = width.roundToInt(),
            height = height.roundToInt(),
            offsetY = view.height * 1 / 7,
        )
    }.apply {
        setContent(composition) {
            DialogLayout {
                Box(
                    modifier = finalModifier
                        .scale(scale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = message.message,
                        style = textStyle,
                    )
                }
            }
        }
    }

    LaunchedEffect(dialog.isShowing) {
        if (!dialog.isShowing) {
            dialog.show()
            showed = true
        }
    }

    DisposableEffect(message) {
        onDispose {
            dialog.dismiss()
            dialog.disposeComposition()
        }
    }

    SideEffect {
        dialog.updateParameters(
            onDismissRequest = onDismiss,
            behaviors = finalBehaviors,
            layoutDirection = layoutDirection
        )
    }
}

@Composable
internal fun Snackbar(
    modifier: Modifier = Modifier,
    message: MessageData,
    onDismiss: () -> Unit,
) {
    SnackbarContent(
        modifier = modifier,
        message = message,
        onDismiss = onDismiss,
    )
}