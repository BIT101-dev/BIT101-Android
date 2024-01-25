package cn.bit101.android.features.common.nav

import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import kotlin.reflect.KClass

sealed interface NavDest {
    val config: NavDestConfig
    val route: String

    companion object {
        private val navDestConfigToNavDest = mutableMapOf(
            NavDestConfig.Schedule to Schedule,
            NavDestConfig.Map to Map,
            NavDestConfig.BIT101Web to BIT101Web,
            NavDestConfig.Gallery to Gallery,
            NavDestConfig.Mine to Mine,
            NavDestConfig.Login to Login,
            NavDestConfig.Post to Post,
            NavDestConfig.Message to Message,
            NavDestConfig.Setting to Setting(),
        )

        private fun fromNavDestConfig(navDestConfig: NavDestConfig): NavDest {
            if (navDestConfigToNavDest.containsKey(navDestConfig)) {
                return navDestConfigToNavDest[navDestConfig]!!
            } else throw IllegalArgumentException("$navDestConfig require arguments")
        }

        /**
         * 通过字符串获取页面，仅限于没有参数的页面
         */
        fun fromRoute(route: String?): NavDest {
            return NavDestConfig.all.find { it.route == route }?.let {
                fromNavDestConfig(it)
            } ?: throw IllegalArgumentException("$route is not a valid route")
        }
    }

    data object Schedule : NavDest {
        override val config = NavDestConfig.Schedule
        override val route = config.route
    }

    data object Map : NavDest {
        override val config = NavDestConfig.Map
        override val route = config.route
    }

    data object BIT101Web : NavDest {
        override val config = NavDestConfig.BIT101Web
        override val route = config.route
    }

    data object Gallery : NavDest {
        override val config = NavDestConfig.Gallery
        override val route = config.route
    }

    data object Mine : NavDest {
        override val config = NavDestConfig.Mine
        override val route = config.route
    }

    data class Setting(
        val initialRoute: String? = null
    ) : NavDest {
        override val config = NavDestConfig.Setting
        override val route = config.route
            .replace("{route}", initialRoute ?: "")
    }

    data object Login: NavDest {
        override val config = NavDestConfig.Login
        override val route = config.route
    }

    data class User(
        val id: Long
    ) : NavDest {
        override val config = NavDestConfig.User
        override val route = config.route
            .replace("{id}", id.toString())
    }

    data class Poster(
        val id: Long
    ) : NavDest {
        override val config = NavDestConfig.Poster
        override val route = config.route
            .replace("{id}", id.toString())
    }

    data object Post : NavDest {
        override val config = NavDestConfig.Post
        override val route = config.route
    }

    data class Edit(
        val id: Long
    ) : NavDest {
        override val config = NavDestConfig.Edit
        override val route = config.route
            .replace("{id}", id.toString())
    }

    data class Report(
        val type: String,
        val id: Long
    ) : NavDest {
        override val config = NavDestConfig.Report

        override val route = config.route
            .replace("{type}", type)
            .replace("{id}", id.toString())
    }

    data object Message : NavDest {
        override val config = NavDestConfig.Message
        override val route = config.route
    }

    data class Web(
        val url: String
    ) : NavDest {
        override val config = NavDestConfig.Web
        override val route = config.route
            .replace("{url}", Uri.encode(url))
    }
}

fun NavHostController.navigate(dest: NavDest) {
    navigate(dest.route)
}

fun NavHostController.navigate(dest: NavDest, options: NavOptions?) {
    navigate(dest.route, options)
}

fun NavHostController.navigate(dest: NavDest, builder: NavOptionsBuilder.() -> Unit) {
    navigate(dest.route, builder)
}