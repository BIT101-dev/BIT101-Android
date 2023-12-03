package cn.bit101.android.ui.setting

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.setting.page.AboutPage
import cn.bit101.android.ui.setting.page.CalendarSettingPage
import cn.bit101.android.ui.setting.page.DDLSettingPage
import cn.bit101.android.ui.setting.page.PagesSettingPage
import cn.bit101.android.ui.setting.page.ThemeSettingPage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    mainController: MainController,
    initialRoute: String = "",
) {
    val topAppBarBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val navController = rememberNavController()

    val settingController = MainController(
        scope = mainController.scope,
        navController = navController,
        snackbarHostState = mainController.snackbarHostState
    )

    val currentEntry = navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val title = when(currentEntry.value?.destination?.route) {
        "index" -> "设置"
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
            LargeTopAppBar (
                title = { Text(text = title) },
                scrollBehavior = topAppBarBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if(navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                mainController.navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(settingController.snackbarHostState) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = initialRoute.ifBlank { "index" }
        ) {
            composable("index") {
                SettingIndexPage(
                    mainController = settingController,
                    paddingValues = paddingValues,
                    onOpenPagesSettingPage = { navController.navigate("pages") },
                    onOpenThemeSettingPage = { navController.navigate("theme") },
                    onOpenCalendarSettingPage = { navController.navigate("calendar") },
                    onOpenAboutPage = { navController.navigate("about") },
                    onOpenDDLSettingPage = { navController.navigate("ddl") },
                )
            }
            composable("pages") {
                PagesSettingPage(
                    mainController = settingController,
                    paddingValues = paddingValues,
                )
            }

            composable("theme") {
                ThemeSettingPage(
                    mainController = settingController,
                    paddingValues = paddingValues,
                )
            }

            composable("calendar") {
                CalendarSettingPage(
                    mainController = settingController,
                    paddingValues = paddingValues,
                )
            }

            composable("about") {
                AboutPage(
                    mainController = settingController,
                    paddingValues = paddingValues,
                )
            }

            composable("ddl") {
                DDLSettingPage(
                    mainController = settingController,
                    paddingValues = paddingValues,
                )
            }
        }
    }
}