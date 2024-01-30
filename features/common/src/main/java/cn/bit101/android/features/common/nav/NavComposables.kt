package cn.bit101.android.features.common.nav

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * 所有动画的时长
 */
const val DURATION_MILLIS = 400

/**
 * 保持
 */
val delayRemainTransition = fadeOut(tween(10, DURATION_MILLIS))

/**
 * 从右侧滑入+淡入
 */
val enterTransition = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(
        durationMillis = DURATION_MILLIS,
        easing = FastOutSlowInEasing
    )
)
//+ fadeIn(
//    animationSpec = tween(
//        durationMillis = DURATION_MILLIS,
//        easing = FastOutSlowInEasing
//    )
//)

/**
 * 向右侧滑出+淡出
 */
val exitTransition = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(
        durationMillis = DURATION_MILLIS,
        easing = FastOutSlowInEasing
    )
)
//+ fadeOut(
//    animationSpec = tween(
//        durationMillis = DURATION_MILLIS,
//        easing = LinearOutSlowInEasing
//    )
//)

data class NavAnimation(
    val enterTransition: @JvmSuppressWildcards (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
    val exitTransition: @JvmSuppressWildcards (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
    val popEnterTransition: @JvmSuppressWildcards (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
    val popExitTransition: @JvmSuppressWildcards (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
) {
    companion object {
        val none = NavAnimation(
            enterTransition = { EnterTransition.None },
            exitTransition = { delayRemainTransition },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { delayRemainTransition },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument>,
    navAnimation: NavAnimation,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = navAnimation.enterTransition,
        exitTransition = navAnimation.exitTransition,
        popEnterTransition = navAnimation.popEnterTransition,
        popExitTransition = navAnimation.popExitTransition,
    ) {
//        val visibleEntries by navController.visibleEntries.collectAsState()
        val currentDest by navController.currentBackStackEntryAsState()

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter {
                    val isCurrentScreen = currentDest?.destination?.route == route
                    !isCurrentScreen
                }
        ) {
            content(it)
        }
    }
}

fun NavGraphBuilder.composableIndex(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Index.route,
        arguments = NavDestConfig.Index.arguments,
        navAnimation = navAnimation,
        navController = navController,
        content = content
    )
}

typealias InitialRoute = String

fun NavGraphBuilder.composableSetting(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry, InitialRoute) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Setting.route,
        arguments = NavDestConfig.Setting.arguments,
        navAnimation = navAnimation,
        navController = navController,
    ) {
        val initialRoute = it.arguments?.getString("route") ?: ""
        content(it, initialRoute)
    }
}

fun NavGraphBuilder.composableLogin(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Login.route,
        arguments = NavDestConfig.Login.arguments,
        navAnimation = navAnimation,
        navController = navController,
        content = content
    )
}

typealias UID = Long

fun NavGraphBuilder.composableUser(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry, UID) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.User.route,
        arguments = NavDestConfig.User.arguments,
        navAnimation = navAnimation,
        navController = navController,
    ) {
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, id)
    }
}

typealias PosterId = Long

fun NavGraphBuilder.composablePoster(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry, PosterId) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Poster.route,
        arguments = NavDestConfig.Poster.arguments,
        navAnimation = navAnimation,
        navController = navController,
    ) {
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, id)
    }
}

fun NavGraphBuilder.composablePost(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Post.route,
        arguments = NavDestConfig.Post.arguments,
        navAnimation = navAnimation,
        navController = navController,
        content = content
    )
}

fun NavGraphBuilder.composableEdit(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry, PosterId) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Edit.route,
        arguments = NavDestConfig.Edit.arguments,
        navAnimation = navAnimation,
        navController = navController,
    ) {
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, id)
    }
}

typealias ReportType = String
typealias ReportId = Long

fun NavGraphBuilder.composableReport(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry, ReportType, ReportId) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Report.route,
        arguments = NavDestConfig.Report.arguments,
        navAnimation = navAnimation,
        navController = navController,
    ) {
        val type = it.arguments?.getString("type") ?: ""
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, type, id)
    }
}

fun NavGraphBuilder.composableMessage(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Message.route,
        arguments = NavDestConfig.Message.arguments,
        navAnimation = navAnimation,
        navController = navController,
        content = content
    )
}

typealias URL = String

fun NavGraphBuilder.composableWeb(
    navAnimation: NavAnimation = NavAnimation.none,
    navController: NavHostController,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry, URL) -> Unit
) {
    animatedComposable(
        route = NavDestConfig.Web.route,
        arguments = NavDestConfig.Web.arguments,
        navAnimation = navAnimation,
        navController = navController,
    ) {
        val url = Uri.decode(it.arguments?.getString("url") ?: "")
        content(it, url)
    }
}