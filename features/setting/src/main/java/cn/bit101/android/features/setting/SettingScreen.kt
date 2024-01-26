package cn.bit101.android.features.setting

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.nav.NavDest
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
    val topAppBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val navController = rememberNavController()

    val onSnackBar: (String) -> Unit = { mainController.snackbar(it) }

    val currentEntry = navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val title = when(currentEntry.value?.destination?.route) {
        "index" -> "设置"
        "account" -> "账号设置"
        "pages" -> "页面设置"
        "theme" -> "外观设置"
        "calendar" -> "课程表设置"
        "about" -> "关于"
        "ddl" -> "DDL设置"
        else -> "设置"
    }

    // 沉浸式状态栏
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar (
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                scrollBehavior = topAppBarBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if(navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                mainController.popBackStack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = initialRoute.ifBlank { "index" },
            enterTransition = {
                fadeIn()
            },
            exitTransition = {
                ExitTransition.None
            },
            popEnterTransition = {
                fadeIn()
            },
            popExitTransition = {
                ExitTransition.None
            }
        ) {
            composable("index") {
                SettingIndexPage(
                    navController = navController,
                    paddingValues = paddingValues,
                )
            }

            composable("account") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    AccountPage(
                        onLogin = { mainController.navigate(NavDest.Login) },
                        onSnackBar = onSnackBar,
                    )
                }
            }

            composable("pages") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    PagesSettingPage(
                        navController = navController,
                        onSnackBar = onSnackBar,
                    )
                }
            }

            composable("theme") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    ThemeSettingPage()
                }
            }

            composable("calendar") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    CalendarSettingPage(onSnackBar = onSnackBar)
                }
            }

            composable("about") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    AboutPage(onSnackBar = onSnackBar)
                }
            }

            composable("ddl") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    DDLSettingPage(onSnackBar = onSnackBar)
                }
            }
        }
    }
}