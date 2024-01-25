package cn.bit101.android.features.common.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.composableSchedule(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.Schedule.route,
        arguments = NavDestConfig.Schedule.arguments,
        content = content
    )
}

fun NavGraphBuilder.composableMap(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.Map.route,
        arguments = NavDestConfig.Map.arguments,
        content = content
    )
}

fun NavGraphBuilder.composableBIT101Web(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.BIT101Web.route,
        arguments = NavDestConfig.BIT101Web.arguments,
        content = content
    )
}

fun NavGraphBuilder.composableGallery(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.Gallery.route,
        arguments = NavDestConfig.Gallery.arguments,
        content = content
    )
}

fun NavGraphBuilder.composableMine(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.Mine.route,
        arguments = NavDestConfig.Mine.arguments,
        content = content
    )
}

typealias InitialRoute = String

fun NavGraphBuilder.composableSetting(
    content: @Composable (NavBackStackEntry, InitialRoute) -> Unit
) {
    composable(
        route = NavDestConfig.Setting.route,
        arguments = NavDestConfig.Setting.arguments,
    ) {
        val initialRoute = it.arguments?.getString("route") ?: ""
        content(it, initialRoute)
    }
}

fun NavGraphBuilder.composableLogin(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.Login.route,
        arguments = NavDestConfig.Login.arguments,
        content = content
    )
}

typealias UID = Long

fun NavGraphBuilder.composableUser(
    content: @Composable (NavBackStackEntry, UID) -> Unit
) {
    composable(
        route = NavDestConfig.User.route,
        arguments = NavDestConfig.User.arguments,
    ) {
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, id)
    }
}

typealias PosterId = Long

fun NavGraphBuilder.composablePoster(
    content: @Composable (NavBackStackEntry, PosterId) -> Unit
) {
    composable(
        route = NavDestConfig.Poster.route,
        arguments = NavDestConfig.Poster.arguments,
    ) {
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, id)
    }
}

fun NavGraphBuilder.composablePost(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.Post.route,
        arguments = NavDestConfig.Post.arguments,
        content = content
    )
}

fun NavGraphBuilder.composableEdit(
    content: @Composable (NavBackStackEntry, PosterId) -> Unit
) {
    composable(
        route = NavDestConfig.Edit.route,
        arguments = NavDestConfig.Edit.arguments,
    ) {
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, id)
    }
}

typealias ReportType = String
typealias ReportId = Long

fun NavGraphBuilder.composableReport(
    content: @Composable (NavBackStackEntry, ReportType, ReportId) -> Unit
) {
    composable(
        route = NavDestConfig.Report.route,
        arguments = NavDestConfig.Report.arguments,
    ) {
        val type = it.arguments?.getString("type") ?: ""
        val id = it.arguments?.getLong("id") ?: 0L
        content(it, type, id)
    }
}

fun NavGraphBuilder.composableMessage(
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = NavDestConfig.Message.route,
        arguments = NavDestConfig.Message.arguments,
        content = content
    )
}

typealias URL = String

fun NavGraphBuilder.composableWeb(
    content: @Composable (NavBackStackEntry, URL) -> Unit
) {
    composable(
        route = NavDestConfig.Web.route,
        arguments = NavDestConfig.Web.arguments,
    ) {
        val url = Uri.decode(it.arguments?.getString("url") ?: "")
        content(it, url)
    }
}