package cn.bit101.android.features.common.nav

import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import cn.bit101.android.config.setting.base.PageShowOnNav
import kotlin.reflect.KClass

sealed interface NavDest {
    val config: NavDestConfig
    val route: String

    data object Index : NavDest {
        override val config = NavDestConfig.Index
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