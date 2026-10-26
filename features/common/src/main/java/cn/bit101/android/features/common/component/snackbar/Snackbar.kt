package cn.bit101.android.features.common.component.snackbar

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.component.DialogLayout
import cn.bit101.android.features.common.component.SnackbarWrapper
import cn.bit101.android.features.common.component.bottomsheet.CoreBottomSheetDefaults
import cn.bit101.android.features.common.component.bottomsheet.DialogSheetBehaviors
import java.util.UUID

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

    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    val paddingHorizontal = 24.dp
    val paddingVertical = 12.dp
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    val availableWidth = minOf(view.width.takeIf { it > 0 } ?: screenWidth, screenWidth)
    val textWidth = availableWidth - with(density) { (32.dp + paddingHorizontal * 2).roundToPx() }
    val measuredText = rememberTextMeasurer().measure(
        text = message.message,
        style = textStyle,
        constraints = Constraints(maxWidth = textWidth.coerceAtLeast(1)),
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
    )
    val width = measuredText.size.width + with(density) { (paddingHorizontal * 2).roundToPx() }
    val height = measuredText.size.height + with(density) { (paddingVertical * 2).roundToPx() }

    val dialog = remember(view, message, width, height) {
        SnackbarWrapper(
            onDismissRequest = onDismiss,
            behaviors = finalBehaviors,
            composeView = view,
            layoutDirection = layoutDirection,
            dialogId = dialogId,
            width = width,
            height = height,
            offsetY = view.height * 1 / 7,
        )
    }.apply {
        setContent(composition) {
            DialogLayout {
                Box(
                    modifier = finalModifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = message.message,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    LaunchedEffect(dialog) {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    DisposableEffect(dialog) {
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
