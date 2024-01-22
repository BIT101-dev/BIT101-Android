package cn.bit101.android.ui.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import cn.bit101.android.features.MainController
import cn.bit101.android.features.setting.SettingIndexPage
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

    val settingController = MainController(
        scope = mainController.scope,
        navController = navController,
        snackbarHostState = mainController.snackbarHostState,
        imageHostState = mainController.imageHostState,
    )

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
                                mainController.navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = initialRoute.ifBlank { "index" }
        ) {
            composable("index") {
                SettingIndexPage(
                    mainController = settingController,
                    paddingValues = paddingValues,
                )
            }

            composable("account") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    AccountPage(
                        mainController = settingController,
                        onLogin = { mainController.navController.navigate("login") }
                    )
                }
            }

            composable("pages") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    PagesSettingPage(mainController = settingController)
                }
            }

            composable("theme") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    ThemeSettingPage(mainController = settingController)
                }
            }

            composable("calendar") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    CalendarSettingPage(mainController = settingController)
                }
            }

            composable("about") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    AboutPage(mainController = settingController)
                }
            }

            composable("ddl") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    DDLSettingPage(mainController = settingController)
                }
            }
        }
    }
}