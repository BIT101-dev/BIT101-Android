package cn.bit101.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.datastore.dataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.MainActivity
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.ui.gallery.GalleryScreen
import cn.bit101.android.ui.login.LoginOrLogoutScreen
import cn.bit101.android.ui.map.MapScreen
import cn.bit101.android.ui.schedule.ScheduleScreen
import cn.bit101.android.ui.setting.SettingScreen
import cn.bit101.android.ui.web.WebScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
fun MainApp(
    vm: MainViewModel = hiltViewModel()
) {
    val enableGallery by vm.enableGalleryFlow.collectAsState(initial = false)

    val homepage by SettingDataStore.settingHomePage.flow.collectAsState(initial = "")

    val systemUiController = rememberSystemUiController()

    val mainController = MainController(
        scope = rememberCoroutineScope(),
        navController = rememberNavController(),
        snackbarHostState = remember { SnackbarHostState() }
    )

    // 底部导航栏路由
    data class Screen(val route: String, val label: String, val icon: ImageVector)

    val routes = if(enableGallery) {
        listOf(
            Screen("schedule", "卷", Icons.Rounded.Event),
            Screen("map", "图", Icons.Rounded.Map),
            Screen("bit101-web", "网", Icons.Rounded.Explore),
            Screen("gallery", "话", Icons.AutoMirrored.Rounded.Chat),
            Screen("setting", "配", Icons.Rounded.Settings),
        )
    } else {
        listOf(
            Screen("schedule", "卷", Icons.Rounded.Event),
            Screen("map", "图", Icons.Rounded.Map),
            Screen("bit101-web", "网", Icons.Rounded.Explore),
            Screen("setting", "配", Icons.Rounded.Settings),
        )
    }

    // 在导航图中才显示底部导航栏
    val showBottomBar = mainController.navController
        .currentBackStackEntryAsState().value?.destination?.route in routes.map { it.route }
    // 底部导航栏的动画状态
    val bottomBarTransitionState =
        remember { MutableTransitionState(false) }
    bottomBarTransitionState.apply { targetState = showBottomBar }


    val currentBackStackEntry by mainController.navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val statusColor = when(currentBackStackEntry?.destination?.route) {
        "bit101-web" -> Color(0xFFFF9A57)
        else -> MaterialTheme.colorScheme.background
    }

    DisposableEffect(statusColor) {
        systemUiController.setStatusBarColor(
            color = statusColor
        )
        onDispose {  }
    }

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
                    val currentDestination = currentBackStackEntry?.destination
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
                                if(!selected) {
                                    // 路由跳转 保证一次返回就能回到主页
                                    mainController.navController.navigate(screen.route) {
                                        popUpTo(mainController.navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            })
                    }
                }
            }
        },
    ) {
        // 用于处理底部导航栏的Padding
        val modifier =
            { nav: NavBackStackEntry ->
                if (nav.destination.route in routes.map { it.route }) Modifier.padding(
                    it
                ) else Modifier
            }

        // 导航
        if(homepage.isNotEmpty()) {
            LaunchedEffect(homepage, enableGallery) {
                if(homepage == "gallery" && !enableGallery) {
                    MainScope().launch {
                        SettingDataStore.settingHomePage.set("schedule")
                    }
                }
            }

            NavHost(
                navController = mainController.navController,
                startDestination = homepage,
            ) {
                composable("schedule") {
                    Box(modifier = modifier(it)) {
                        ScheduleScreen(mainController)
                    }
                }
                composable("login") {
                    Box(modifier = modifier(it)) {
                        LoginOrLogoutScreen(mainController)
                    }
                }
                composable("map") {
                    Box(modifier = modifier(it)) {
                        MapScreen()
                    }
                }
                composable("bit101-web") {
                    Box(modifier = modifier(it)) {
                        WebScreen(mainController)
                    }
                }
                composable("gallery") {
                    Box(modifier = modifier(it)) {
                        GalleryScreen(mainController)
                    }
                }
                composable("setting") {
                    Box(modifier = modifier(it)) {
                        SettingScreen(mainController)
                    }
                }
            }
        }

    }
}