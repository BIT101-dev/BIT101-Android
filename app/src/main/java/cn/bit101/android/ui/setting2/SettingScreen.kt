package cn.bit101.android.ui.setting2

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.setting2.page.AboutPage
import cn.bit101.android.ui.setting2.page.CalendarSettingPage
import cn.bit101.android.ui.setting2.page.DDLSettingPageContent
import cn.bit101.android.ui.setting2.page.PagesSettingPageContent
import cn.bit101.android.ui.setting2.page.ThemeSettingPageContent
import cn.bit101.android.utils.ColorUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingContent(
//    mainController: MainController,
    onBack: () -> Unit = {},
) {
    val topAppBarBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = ColorUtils.isLightColor(MaterialTheme.colorScheme.background)
    )

    val navController = rememberNavController()

    val mainController = MainController(
        scope = rememberCoroutineScope(),
        navController = navController,
        snackbarHostState = remember { SnackbarHostState() }
    )

    val currentEntry = navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val title = when(currentEntry.value?.destination?.route) {
        "index" -> "设置"
        "pages" -> "页面设置"
        "theme" -> "外观设置"
        "calendar" -> "课程表设置"
        else -> "设置"
    }

    // 沉浸式状态栏
    Scaffold(
        topBar = {
            LargeTopAppBar (
                title = { Text(text = title) },
                scrollBehavior = topAppBarBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(mainController.snackbarHostState) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "index"
        ) {
            composable("index") {
                SettingIndexPage(
                    mainController = mainController,
                    paddingValues = paddingValues,
                    nestedScrollConnection = topAppBarBehavior.nestedScrollConnection,
                    onOpenPagesSettingPage = { navController.navigate("pages") },
                    onOpenThemeSettingPage = { navController.navigate("theme") },
                    onOpenCalendarSettingPage = { navController.navigate("calendar") },
                    onOpenAboutPage = { navController.navigate("about") },
                    onOpenDDLSettingPage = { navController.navigate("ddl") },
                )
            }
            composable("pages") {
                PagesSettingPageContent(
                    mainController = mainController,
                    paddingValues = paddingValues,
                    nestedScrollConnection = topAppBarBehavior.nestedScrollConnection,
                )
            }

            composable("theme") {
                ThemeSettingPageContent(
                    mainController = mainController,
                    paddingValues = paddingValues,
                    nestedScrollConnection = topAppBarBehavior.nestedScrollConnection,
                )
            }

            composable("calendar") {
                CalendarSettingPage(
                    mainController = mainController,
                    paddingValues = paddingValues,
                    nestedScrollConnection = topAppBarBehavior.nestedScrollConnection,
                )
            }

            composable("about") {
                AboutPage(
                    mainController = mainController,
                    paddingValues = paddingValues,
                    nestedScrollConnection = topAppBarBehavior.nestedScrollConnection,
                )
            }

            composable("ddl") {
                DDLSettingPageContent(
                    mainController = mainController,
                    paddingValues = paddingValues,
                    nestedScrollConnection = topAppBarBehavior.nestedScrollConnection,
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    SettingContent()
}