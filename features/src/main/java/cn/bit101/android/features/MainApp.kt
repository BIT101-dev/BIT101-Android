package cn.bit101.android.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.config.setting.base.toPageData
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.NavigationBar
import cn.bit101.android.features.common.component.image.ImageHost
import cn.bit101.android.features.common.component.image.rememberImageHostState
import cn.bit101.android.features.common.component.snackbar.SnackbarHost
import cn.bit101.android.features.common.component.snackbar.rememberSnackbarState
import cn.bit101.android.features.common.helper.NavBarHeight
import cn.bit101.android.features.common.helper.getAppVersion
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.common.nav.NavDestConfig
import cn.bit101.android.features.common.utils.ColorUtils
import cn.bit101.android.features.versions.UpdateDialog
import cn.bit101.android.features.versions.VersionDialog
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private data class SystemUIConfig(
    val statusBarColor: Color,
    val statusBarDarkIcon: Boolean,
)

@Composable
private fun getSystemUI(destConfig: NavDestConfig?): SystemUIConfig {
    val statusBarColor = when(destConfig) {
        NavDestConfig.BIT101Web, NavDestConfig.Web, NavDestConfig.Message -> Color(0xFFFF9A57)
        NavDestConfig.Setting -> Color.Transparent
        NavDestConfig.User, NavDestConfig.Mine -> Color.Transparent
        NavDestConfig.Post, NavDestConfig.Edit -> Color.Transparent
        NavDestConfig.Report -> Color.Transparent
        NavDestConfig.Gallery, NavDestConfig.Poster -> Color.Transparent
        else -> MaterialTheme.colorScheme.background
    }
    val statusBarDarkIcon = when(statusBarColor) {
        Color.Transparent -> ColorUtils.isLightColor(MaterialTheme.colorScheme.background)
        else -> ColorUtils.isLightColor(statusBarColor)
    }

    return SystemUIConfig(statusBarColor, statusBarDarkIcon)
}

@Composable
internal fun MainApp() {
    val vm: MainViewModel = hiltViewModel()

    val ctx = LocalContext.current

    val systemUiController = rememberSystemUiController()

    val navController = rememberNavController()

    val mainController = MainController(
        scope = rememberCoroutineScope(),
        navController = navController,
        snackbarHostState = rememberSnackbarState(
            scope = rememberCoroutineScope()
        ),
        imageHostState = rememberImageHostState()
    )

    // 当前路由
    val currentDestConfig by mainController.currentDestConfigFlow.collectAsState(initial = null)

    // 状态栏颜色
    val systemUIConfig = getSystemUI(currentDestConfig)
    LaunchedEffect(systemUIConfig) {
        systemUiController.setStatusBarColor(
            color = systemUIConfig.statusBarColor,
            darkIcons = systemUIConfig.statusBarDarkIcon,
        )
    }

    // 底部导航栏路由
    data class Screen(
        val dest: NavDest,
        val label: String,
        val icon: ImageVector
    )

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
            dest = NavDest.fromRoute(page.value),
            label = page.name,
            icon = icon
        )
    }

    // 在导航图中才显示底部导航栏
    val showBottomBar = currentDestConfig in routes.map { it.dest.config }

    // 底部导航栏的动画状态
    val bottomBarTransitionState =
        remember { MutableTransitionState(false) }
    bottomBarTransitionState.apply { targetState = showBottomBar }

    val navBarColor = if(showBottomBar) MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
    else when(currentDestConfig) {
        NavDestConfig.Post, NavDestConfig.Edit -> MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        NavDestConfig.Report -> MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
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
                    routes.forEach { screen ->
                        val selected = currentDestConfig == screen.dest.config
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
                                    mainController.navigate(screen.dest) {
                                        popUpTo(mainController.startDestId) {
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
        val navGraph = rememberMainNavGraph(
            mainController = mainController,
            navController = navController,
            paddingValues = paddingValues,
            loginStatus = loginStatus,
            startDestination = homePage!!.toPageData().value,
        )

        NavHost(
            navController = navController,
            graph = navGraph
        )

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