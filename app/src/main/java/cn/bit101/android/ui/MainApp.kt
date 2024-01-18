package cn.bit101.android.ui

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import cn.bit101.android.BuildConfig
import cn.bit101.android.manager.base.PageShowOnNav
import cn.bit101.android.manager.base.toNameAndValue
import cn.bit101.android.ui.common.NavBarHeight
import cn.bit101.android.ui.component.image.ImageHost
import cn.bit101.android.ui.component.image.rememberImageHostState
import cn.bit101.android.ui.component.navigationbar.NavigationBar
import cn.bit101.android.ui.component.snackbar.SnackbarHost
import cn.bit101.android.ui.component.snackbar.rememberSnackbarState
import cn.bit101.android.ui.gallery.index.GalleryScreen
import cn.bit101.android.ui.gallery.postedit.PostEditScreen
import cn.bit101.android.ui.gallery.poster.PosterScreen
import cn.bit101.android.ui.gallery.report.ReportScreen
import cn.bit101.android.ui.login.LoginOrLogoutScreen
import cn.bit101.android.ui.map.MapScreen
import cn.bit101.android.ui.message.MessageScreen
import cn.bit101.android.ui.mine.MineScreen
import cn.bit101.android.ui.schedule.ScheduleScreen
import cn.bit101.android.ui.setting.SettingScreen
import cn.bit101.android.ui.user.UserScreen
import cn.bit101.android.ui.versions.UpdateDialog
import cn.bit101.android.ui.versions.VersionDialog
import cn.bit101.android.ui.web.WebScreen
import cn.bit101.android.utils.ColorUtils
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

    val mainController = MainController(
        scope = rememberCoroutineScope(),
        navController = rememberNavController(),
        snackbarHostState = rememberSnackbarState(scope = rememberCoroutineScope()),
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
            else -> ColorUtils.isLightColor(statusColor)
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
        val page = it.toNameAndValue()

        val icon = when(it) {
            PageShowOnNav.Schedule -> Icons.Rounded.Event
            PageShowOnNav.Map -> Icons.Rounded.Map
            PageShowOnNav.BIT101Web -> Icons.Rounded.Explore
            PageShowOnNav.Gallery -> Icons.Rounded.Chat
            PageShowOnNav.Mine -> Icons.Rounded.AccountCircle
        }

        Screen(
            route = page.value,
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

    if(lastVersion!! < BuildConfig.VERSION_CODE) {
        VersionDialog(
            onConfirm = vm::logout,
            onDismiss = vm::setLastVersion
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
            startDestination = homePage!!.toNameAndValue().value,
        ) {
            composable("schedule") {
                WithLoginStatus(mainController, loginStatus) {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ScheduleScreen(mainController)
                    }
                }
            }

            composable("login") {
                LoginOrLogoutScreen(mainController)
            }

            composable("map") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    MapScreen()
                }
            }
            composable("bit101-web") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    WebScreen(mainController)
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
                    WebScreen(mainController, url = url)
                }
            }

            composable("gallery") {
                WithLoginStatus(mainController, loginStatus) {
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        GalleryScreen(mainController = mainController)
                    }
                }
            }
            composable("mine") {
                Box(
                    modifier = Modifier.padding(
                        bottom = paddingValues.calculateBottomPadding()
                    )
                ) {
                    MineScreen(mainController)
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
                SettingScreen(
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
                    UserScreen(
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
                    PosterScreen(
                        mainController = mainController,
                        id = it.arguments?.getLong("id") ?: 0L,
                    )
                }
            }

            composable("post") {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    PostEditScreen(
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
                    PostEditScreen(
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
                    ReportScreen(
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
                    MessageScreen(mainController = mainController)
                }
            }
        }

        ImageHost(
            modifier = Modifier.fillMaxSize(),
            state = mainController.imageHostState,
            onOpenUrl = { mainController.openUrl(it, ctx) },
        )

        SnackbarHost(
            state = mainController.snackbarHostState
        )
    }
}