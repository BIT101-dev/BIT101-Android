package cn.bit101.android.features.setting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.common.nav.enterTransition
import cn.bit101.android.features.common.nav.exitTransition
import cn.bit101.android.features.common.nav.popEnterTransition
import cn.bit101.android.features.common.nav.popExitTransition
import cn.bit101.android.features.setting.component.SettingPage
import cn.bit101.android.features.setting.page.AboutPage
import cn.bit101.android.features.setting.page.AccountPage
import cn.bit101.android.features.setting.page.CalendarSettingPage
import cn.bit101.android.features.setting.page.DDLSettingPage
import cn.bit101.android.features.setting.page.PagesSettingPage
import cn.bit101.android.features.setting.page.ThemeSettingPage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    mainController: MainController,
    initialRoute: String = "",
) {
    val navController = rememberNavController()
    val onSnackBar: (String) -> Unit = { mainController.snackbar(it) }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = initialRoute.ifBlank { "index" },
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition },
    ) {
        composable("index") {
            SettingPage(
                mainController = mainController,
                title = "设置",
                navController = navController,
            ) {
                SettingIndexPage(navController)
            }
        }

        composable("account") {
            SettingPage(
                mainController = mainController,
                title = "账号设置",
                navController = navController,
            ) {
                AccountPage(
                    onLogin = { mainController.navigate(NavDest.Login) },
                    onSnackBar = onSnackBar,
                )
            }
        }

        composable("pages") {
            SettingPage(
                mainController = mainController,
                title = "页面设置",
                navController = navController,
            ) {
                PagesSettingPage(
                    navController = navController,
                    onSnackBar = onSnackBar,
                )
            }
        }

        composable("theme") {
            SettingPage(
                mainController = mainController,
                title = "外观设置",
                navController = navController,
            ) {
                ThemeSettingPage()
            }
        }

        composable("calendar") {
            SettingPage(
                mainController = mainController,
                title = "课程表设置",
                navController = navController,
            ) {
                CalendarSettingPage(onSnackBar = onSnackBar)
            }
        }

        composable("about") {
            SettingPage(
                mainController = mainController,
                title = "关于",
                navController = navController,
            ) {
                AboutPage(onSnackBar = onSnackBar)
            }
        }

        composable("ddl") {
            SettingPage(
                mainController = mainController,
                title = "DDL设置",
                navController = navController,
            ) {
                DDLSettingPage(onSnackBar = onSnackBar)
            }
        }
    }
}