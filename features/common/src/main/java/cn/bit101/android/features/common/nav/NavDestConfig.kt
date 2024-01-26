package cn.bit101.android.features.common.nav

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed interface NavDestConfig {
    val route: String
    val arguments: List<NamedNavArgument>

    companion object {
        val all = listOf(
            Index,
            Setting,
            Login,
            User,
            Poster,
            Post,
            Edit,
            Report,
            Message,
            Web,
        )

        fun fromRoute(route: String?): NavDestConfig? {
            return all.find { it.route == route }
        }
    }

    data object Index : NavDestConfig {
        override val route: String = "index"
        override val arguments: List<NamedNavArgument> = emptyList()
    }

    data object Setting : NavDestConfig {
        override val route: String = "setting?route={route}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument("route") {
                type = NavType.StringType
                nullable = true
            }
        )
    }

    data object Login : NavDestConfig {
        override val route: String = "login"
        override val arguments: List<NamedNavArgument> = emptyList()
    }

    data object User : NavDestConfig {
        override val route: String = "user/{id}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument("id") { type = NavType.LongType },
        )
    }

    data object Poster : NavDestConfig {
        override val route: String = "poster/{id}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument("id") { type = NavType.LongType },
        )
    }

    data object Post : NavDestConfig {
        override val route: String = "post"
        override val arguments: List<NamedNavArgument> = emptyList()
    }

    data object Edit : NavDestConfig {
        override val route: String = "edit/{id}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument("id") { type = NavType.LongType },
        )
    }

    data object Report : NavDestConfig {
        override val route: String = "report/{type}/{id}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument("type") { type = NavType.StringType },
            navArgument("id") { type = NavType.LongType },
        )
    }

    data object Message : NavDestConfig {
        override val route: String = "message"
        override val arguments: List<NamedNavArgument> = emptyList()
    }

    data object Web : NavDestConfig {
        override val route: String = "web/{url}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument("url") { type = NavType.StringType },
        )
    }
}