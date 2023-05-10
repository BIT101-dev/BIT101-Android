package cn.bit101.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.net.updateStatus
import cn.bit101.android.ui.LoginOrLogout
import cn.bit101.android.ui.MapComponent
import cn.bit101.android.ui.Schedule
import cn.bit101.android.ui.theme.BIT101Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BIT101Theme {
                MainContent()
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

    fun route(route: String) {
        // 路由跳转 保证唯一
        navController.navigate(route){
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        Screen("map", "图", Icons.Rounded.Map)
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
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                            label = { Text(text = screen.label) },
                            selected = selected,
                            onClick = {
                                mainController.route(screen.route)
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
                Box(modifier = modifier(it)) {
                    Schedule(mainController)
                }
            }
            composable("login") {
                Box(modifier = modifier(it)) {
                    LoginOrLogout(mainController)
                }
            }
            composable("map") {
                Box(modifier = modifier(it)) {
                    MapComponent()
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