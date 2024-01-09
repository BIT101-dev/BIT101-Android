package cn.bit101.android.ui.component.common

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable

@Composable
fun <T> AnimatedPage(
    page: T,
    isMainPage: Boolean,
    label: String,
    onDismiss: () -> Unit,
    content: @Composable (AnimatedContentScope.(T) -> Unit)
) {
    BackHandler(!isMainPage) {
        onDismiss()
    }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            // 推入的效果
            if(isMainPage) {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(200)
                    ),
                    initialContentExit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(200)
                    ),
                )
            } else {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(200)
                    ),
                    initialContentExit = slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(200)
                    ),
                )
            }
        },
        label = label,
        content = content,
    )
}