package cn.bit101.android.features

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.config.setting.base.toPageData
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.NavigationBar
import cn.bit101.android.features.common.component.image.rememberImageHostState
import cn.bit101.android.features.common.component.snackbar.rememberSnackbarState
import cn.bit101.android.features.common.helper.NavBarHeight
import cn.bit101.android.features.common.helper.getAppVersion
import cn.bit101.android.features.versions.UpdateDialog
import cn.bit101.android.features.versions.VersionDialog
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
private fun WithLoginStatus(
    mainController: MainController,
    status: Boolean?,
    content: @Composable () -> Unit
) {
    when (status) {
        null -> {
            // 未知状态
        }
        true -> {
            content()
        }
        false -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    mainController.navigate("login") {
                        launchSingleTop = true
                    }
                }) {
                    Text("登录")
                }
            }
        }
    }
}

@Composable
fun MainApp(
    vm: MainViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current

    val systemUiController = rememberSystemUiController()

    val mainController = cn.bit101.android.features.common.MainController(
        scope = rememberCoroutineScope(),
        navController = rememberNavController(),
        snackbarHostState = rememberSnackbarState(
            scope = rememberCoroutineScope()
        ),
        imageHostState = rememberImageHostState()
    )

    // 当前路由
    val currentBackStackEntry by mainController.navController.currentBackStackEntryFlow.collectAsState(
        initial = null
    )

    val isDarkMode = MaterialTheme.colorScheme.background.luminance() > 0.5f

    val statusColor = when (currentBackStackEntry?.destination?.route) {
        "bit101-web", "web/{url}", "message" -> Color(0xFFFF9A57)
        "setting?route={route}" -> Color.Transparent
        "user/{id}", "mine" -> Color.Transparent
        "post", "edit/{id}" -> Color.Transparent
        "report/{type}/{id}" -> Color.Transparent
        "gallery", "poster/{id}" -> Color.Transparent
        else -> MaterialTheme.colorScheme.background
    }

    LaunchedEffect(statusColor) {
        val darkIcons = when (statusColor) {
            Color.Transparent -> isDarkMode
            else -> cn.bit101.android.features.common.utils.ColorUtils.isLightColor(statusColor)
        }

        systemUiController.setStatusBarColor(
            color = statusColor,
            darkIcons = darkIcons,
        )
    }

    // 底部导航栏路由
    data class Screen(val route: String, val label: String, val icon: ImageVector)

    val pages by vm.allPagesFlow.collectAsState(initial = null)
    val homePage by vm.homePageFlow.collectAsState(initial = null)
    val hiddenPages by vm.hidePagesFlow.collectAsState(initial = null)

    if (pages == null || homePage == null || hiddenPages == null) {
        return
    }

    val routes = pages!!.filter { !hiddenPages!!.contains(it) }.map {
        val page = it.toPageData()

        val icon = when(it) {
            PageShowOnNav.Schedule -> Icons.Rounded.Event
            PageShowOnNav.Map -> Icons.Rounded.Map
            PageShowOnNav.BIT101Web -> Icons.Rounded.Explore
            PageShowOnNav.Gallery -> Icons.Rounded.Chat
            PageShowOnNav.Mine -> Icons.Rounded.AccountCircle
        }

        Screen(
            route = page.route,
            label = page.name,
            icon = icon
        )
    }

    // 在导航图中才显示底部导航栏
    val showBottomBar = mainController.navController
        .currentBackStackEntryAsState().value?.destination?.route in routes.map { it.route }

    // 底部导航栏的动画状态
    val bottomBarTransitionState =
        remember { MutableTransitionState(false) }
    bottomBarTransitionState.apply { targetState = showBottomBar }

    val navBarColor = if(showBottomBar) MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
    else when(currentBackStackEntry?.destination?.route) {
        "post", "edit/{id}" -> MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        "poster/{id}" -> MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
        else -> MaterialTheme.colorScheme.background
    }

    LaunchedEffect(navBarColor) {
        systemUiController.setNavigationBarColor(navBarColor)
    }

    val loginStatus by vm.loginStatusFlow.collectAsState(initial = null)

    val lastVersion by vm.lastVersionFlow.collectAsState(initial = null)
    if(lastVersion == null) return

    val appVersion = getAppVersion(ctx)

    if(lastVersion!! < appVersion.versionNumber) {
        VersionDialog(
            onConfirm = vm::logout,
            onDismiss = { vm.setLastVersion(appVersion.versionNumber) }
        )
    }

    val autoDetectUpgrade by vm.autoDetectUpgradeFlow.collectAsState(initial = false)
    if (autoDetectUpgrade) {
        UpdateDialog()
    }

    Scaffold(
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
                NavigationBar(height = NavBarHeight) {
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
                                if (!selected) {
                                    // 路由跳转 保证一次返回就能回到主页
                                    mainController.navController.navigate(screen.route) {
                                        popUpTo(mainController.navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = mainController.navController,
            startDestination = homePage!!.toPageData().route,
        ) {
            composable("schedule") {
                WithLoginStatus(mainController, loginStatus) {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        cn.bit101.android.features.schedule.ScheduleScreen(mainController)
                    }
                }
            }

            composable("login") {
                cn.bit101.android.features.login.LoginOrLogoutScreen(mainController)
            }

            composable("map") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    cn.bit101.android.features.map.MapScreen()
                }
            }
            composable("bit101-web") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    cn.bit101.android.features.web.WebScreen(mainController)
                }
            }

            composable(
                route = "web/{url}",
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType },
                ),
            ) {
                val url = Uri.decode(it.arguments?.getString("url") ?: "")
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .statusBarsPadding()
                ) {
                    cn.bit101.android.features.web.WebScreen(mainController, url = url)
                }
            }

            composable("gallery") {
                WithLoginStatus(mainController, loginStatus) {
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        cn.bit101.android.features.gallery.GalleryScreen(mainController = mainController)
                    }
                }
            }
            composable("mine") {
                Box(
                    modifier = Modifier.padding(
                        bottom = paddingValues.calculateBottomPadding()
                    )
                ) {
                    cn.bit101.android.features.mine.MineScreen(mainController)
                }
            }

            composable(
                route = "setting?route={route}",
                // 可选参数：打开后的页面
                arguments = listOf(
                    navArgument("route") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) {
                val route = it.arguments?.getString("route") ?: ""
                cn.bit101.android.features.setting.SettingScreen(
                    mainController = mainController,
                    initialRoute = route
                )
            }

            composable(
                route = "user/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                val id = it.arguments?.getLong("id") ?: 0L
                Box(modifier = Modifier.navigationBarsPadding()) {
                    cn.bit101.android.features.user.UserScreen(
                        mainController = mainController,
                        id = id,
                    )
                }
            }

            composable(
                route = "poster/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    cn.bit101.android.features.poster.PosterScreen(
                        mainController = mainController,
                        id = it.arguments?.getLong("id") ?: 0L,
                    )
                }
            }

            composable("post") {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    cn.bit101.android.features.postedit.PostEditScreen(
                        mainController = mainController,
                    )
                }
            }

            composable(
                route = "edit/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                val id = it.arguments?.getLong("id") ?: 0L
                Box(modifier = Modifier.navigationBarsPadding()) {
                    cn.bit101.android.features.postedit.PostEditScreen(
                        mainController = mainController,
                        id = id,
                    )
                }
            }

            composable(
                route = "report/{type}/{id}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                val type = it.arguments?.getString("type") ?: ""
                val id = it.arguments?.getLong("id") ?: 0L
                Box(modifier = Modifier.navigationBarsPadding()) {
                    cn.bit101.android.features.report.ReportScreen(
                        mainController = mainController,
                        objType = type,
                        id = id,
                    )
                }
            }

            composable(route = "message") {
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    cn.bit101.android.features.message.MessageScreen(mainController = mainController)
                }
            }
        }

        cn.bit101.android.features.common.component.image.ImageHost(
            modifier = Modifier.fillMaxSize(),
            state = mainController.imageHostState,
            onOpenUrl = { mainController.openUrl(it, ctx) },
        )

        cn.bit101.android.features.common.component.snackbar.SnackbarHost(
            state = mainController.snackbarHostState
        )
    }
}