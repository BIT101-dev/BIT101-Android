package cn.bit101.android.ui.component.snackbar

import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import cn.bit101.android.ui.component.bottomsheet.CoreBottomSheetDefaults
import cn.bit101.android.ui.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.ui.component.common.DialogLayout
import cn.bit101.android.ui.component.common.SnackbarWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun SnackbarContent(
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
    val scale by animateFloatAsState(
        targetValue = if(showed) 1f else 0f,
        label = "",
        animationSpec = tween(100),
    )

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
            width =  width.roundToInt(),
            height = height.roundToInt(),
            offsetY = view.height * 1 / 7,
        )
    }.apply {
        setContent(composition) {
            DialogLayout {
                Log.i("SnackbarContent", "scale: $scale")
                Box(
                    modifier = finalModifier
                        .scale(scale)
                        .shadow(2.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
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
            runBlocking {
                showed = false
                delay(300)
                dialog.dismiss()
                dialog.disposeComposition()
            }
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
fun Snackbar(
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