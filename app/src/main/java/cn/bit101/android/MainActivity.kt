package cn.bit101.android

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.database.DataStore
import cn.bit101.android.net.updateStatus
import cn.bit101.android.ui.BIT101Web
import cn.bit101.android.ui.LoginOrLogout
import cn.bit101.android.ui.MapComponent
import cn.bit101.android.ui.Schedule
import cn.bit101.android.ui.Setting
import cn.bit101.android.ui.theme.BIT101Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        var window: Window? = null
    }

    // 判断一个颜色是否是浅色
    private fun isLightColor(c: Color): Boolean {
        val color = c.toArgb()
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color shr 0 and 0xFF
        val grayLevel = 0.2126 * red + 0.7152 * green + 0.0722 * blue
        return grayLevel >= 192
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.window = window

        setContent {
            BIT101Theme {
                // 设置状态栏文字颜色
                WindowCompat.getInsetsController(
                    window,
                    LocalView.current
                ).isAppearanceLightStatusBars = isLightColor(MaterialTheme.colorScheme.background)

                // 设置导航栏颜色与应用内导航栏匹配
                window?.navigationBarColor =
                    MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
                        .toArgb()
                MainContent()
            }
        }

        // 设置屏幕旋转
        MainScope().launch {
            DataStore.settingRotateFlow.collect {
                requestedOrientation = if (it) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }

        MainScope().launch {
            updateStatus()
        }

    }
}

class MainController(
    val scope: CoroutineScope,
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState
) {
    fun snackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
fun MainContent() {
    val mainController = MainController(
        scope = rememberCoroutineScope(),
        navController = rememberNavController(),
        snackbarHostState = remember { SnackbarHostState() }
    )

    // 底部导航栏路由
    data class Screen(val route: String, val label: String, val icon: ImageVector)

    val routes = listOf(
        Screen("schedule", "卷", Icons.Rounded.Event),
        Screen("map", "图", Icons.Rounded.Map),
        Screen("bit101-web", "网", Icons.Rounded.Explore),
        Screen("setting", "配", Icons.Rounded.Settings)
    )
    // 在导航图中才显示底部导航栏
    val showBottomBar = mainController.navController
        .currentBackStackEntryAsState().value?.destination?.route in routes.map { it.route }
    // 底部导航栏的动画状态
    val bottomBarTransitionState =
        remember { MutableTransitionState(false) }
    bottomBarTransitionState.apply { targetState = showBottomBar }

    Scaffold(
        snackbarHost = { SnackbarHost(mainController.snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visibleState = bottomBarTransitionState,
                enter = slideIn(
                    initialOffset = { IntOffset(0, it.height) },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                ),
                exit = slideOut(
                    targetOffset = { IntOffset(0, it.height) },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                )
            ) {
                // 底部导航栏
                NavigationBar {
                    val navBackStackEntry by mainController.navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    routes.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(text = screen.label) },
                            selected = selected,
                            onClick = {
                                // 路由跳转 保证一次返回就能回到主页
                                mainController.navController.navigate(screen.route) {
                                    popUpTo(mainController.navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                    }
                }
            }
        }
    ) {
        // 用于处理底部导航栏的Padding
        val modifier =
            { nav: NavBackStackEntry ->
                if (nav.destination.route in routes.map { it.route }) Modifier.padding(
                    it
                ) else Modifier
            }

        // 导航
        NavHost(
            navController = mainController.navController,
            startDestination = "schedule",
        ) {
            composable("schedule") {
                MainActivity.window?.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
                Box(modifier = modifier(it)) {
                    Schedule(mainController)
                }
            }
            composable("login") {
                MainActivity.window?.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
                Box(modifier = modifier(it)) {
                    LoginOrLogout(mainController)
                }
            }
            composable("map") {
                MainActivity.window?.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
                Box(modifier = modifier(it)) {
                    MapComponent()
                }
            }
            composable("bit101-web") {
                MainActivity.window?.statusBarColor = Color(0xFFFF9A57).toArgb()
                Box(modifier = modifier(it)) {
                    BIT101Web()
                }
            }
            composable("setting") {
                MainActivity.window?.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
                Box(modifier = modifier(it)) {
                    Setting(mainController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BIT101Theme {
        MainContent()
    }
}