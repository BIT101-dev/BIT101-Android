package cn.bit101.android.ui

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
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
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.ui.gallery.image.ImageScreen
import cn.bit101.android.ui.gallery.index.GalleryScreen
import cn.bit101.android.ui.gallery.message.MessageScreen
import cn.bit101.android.ui.gallery.postedit.PostEditScreen
import cn.bit101.android.ui.gallery.poster.PosterScreen
import cn.bit101.android.ui.gallery.report.ReportScreen
import cn.bit101.android.ui.gallery.user.UserScreen
import cn.bit101.android.ui.login.LoginOrLogoutScreen
import cn.bit101.android.ui.map.MapScreen
import cn.bit101.android.ui.schedule.ScheduleScreen
import cn.bit101.android.ui.mine.MineScreen
import cn.bit101.android.ui.setting.SettingScreen
import cn.bit101.android.ui.web.WebScreen
import cn.bit101.android.utils.PageUtils
import cn.bit101.api.model.common.Image
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun MainApp(
    vm: MainViewModel = hiltViewModel()
) {
    val systemUiController = rememberSystemUiController()

    val mainController = MainController(
        scope = rememberCoroutineScope(),
        navController = rememberNavController(),
        snackbarHostState = remember { SnackbarHostState() }
    )

    // 底部导航栏路由
    data class Screen(val route: String, val label: String, val icon: ImageVector)

    val pagesStr by SettingDataStore.settingPageOrder.flow.collectAsState(initial = null)
    val homePageStr by SettingDataStore.settingHomePage.flow.collectAsState(initial = null)
    val hiddenPagesStr by SettingDataStore.settingPageVisible.flow.collectAsState(initial = null)

    if(pagesStr == null || homePageStr == null || hiddenPagesStr == null) {
        return
    }

    val pages = PageUtils.getReorderedPages(pagesStr!!)
    val homePage = PageUtils.getPage(homePageStr!!).value
    val hiddenPages = PageUtils.getPages(hiddenPagesStr!!).map { it.value }

    val routes = pages.filter { it.value !in hiddenPages }.map {
        Screen(
            route = it.value,
            label = it.name,
            icon = PageUtils.iconsMap[it.value] ?: Icons.Rounded.Error
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

    val isDarkMode = MaterialTheme.colorScheme.background.luminance() > 0.5f

    val statusColor = when(currentBackStackEntry?.destination?.route) {
        "bit101-web" -> Color(0xFFFF9A57)
        "setting?route={route}" -> Color.Transparent
        else -> MaterialTheme.colorScheme.background
    }

    LaunchedEffect(statusColor) {
        val darkIcons = when(statusColor) {
            Color.Transparent -> isDarkMode
            else -> statusColor.luminance() > 0.5f
        }

        systemUiController.setStatusBarColor(
            color = statusColor,
            darkIcons = darkIcons,
        )
    }

    val onOpenImage: (Image) -> Unit = { image ->
        val encodedUrl = URLEncoder.encode(image.url, "UTF-8")
        mainController.navController.navigate("image/$encodedUrl")
    }

    val onOpenImages: (Int, List<Image>) -> Unit = { index, images ->
        val encodedUrls = images.map { URLEncoder.encode(it.url, "UTF-8") }.joinToString(",")
        Log.i("GalleryScreen", "images/$encodedUrls/$index")

        mainController.navController.navigate("images/$encodedUrls/$index")
    }


    val navBarHeight = 70f

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
                NavigationBar(
                    modifier = Modifier
                        .height(navBarHeight.dp)
                ) {
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
                            }
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = mainController.navController,
            startDestination = homePage.ifBlank { "schedule" },
        ) {
            composable("schedule") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    ScheduleScreen(mainController)
                }
            }

            composable("login") {
                LoginOrLogoutScreen(mainController)
            }
            composable("map") {
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    MapScreen()
                }
            }
            composable("bit101-web") {
                Box(modifier = Modifier.padding(paddingValues)) {
                    WebScreen(mainController)
                }
            }

            composable("gallery") {
                Box(
                    modifier = Modifier
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            bottom = navBarHeight.dp
                        )
                        .navigationBarsPadding()
                ) {
                    GalleryScreen(
                        mainController = mainController,
                        onOpenImages = onOpenImages,
                    )
                }
            }
            composable("mine") {
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
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
                route = "images/{urls}/{index}",
                arguments = listOf(
                    navArgument("urls") { type = NavType.StringType },
                    navArgument("index") { type = NavType.IntType },
                ),
            ) {
                val urls = (it.arguments?.getString("urls")?.split(",") ?: emptyList()).map { encodedUrl ->
                    URLDecoder.decode(encodedUrl, "UTF-8")
                }
                val index = it.arguments?.getInt("index") ?: 0
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    ImageScreen(
                        urls = urls,
                        index = index
                    )
                }
            }

            composable(
                route = "image/{url}",
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType },
                ),
            ) {
                val encodedUrl = it.arguments?.getString("url") ?: ""
                val url = URLDecoder.decode(encodedUrl, "UTF-8")
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    ImageScreen(url)
                }
            }

            composable(
                route = "user/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                val id = it.arguments?.getLong("id") ?: 0L
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    UserScreen(
                        mainController = mainController,
                        id = id,
                        onOpenImage = onOpenImage,
                        onOpenImages = onOpenImages,
                    )
                }
            }

            composable(
                route = "poster/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    PosterScreen(
                        mainController = mainController,
                        id = it.arguments?.getLong("id") ?: 0L,
                        onOpenImage = onOpenImage,
                        onOpenImages = onOpenImages,
                    )
                }
            }

            composable("post") {
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    PostEditScreen(
                        mainController = mainController,
                        onOpenImage = onOpenImage,
                    )
                }
            }

            composable("post") {
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    PostEditScreen(
                        mainController = mainController,
                        onOpenImage = onOpenImage,
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
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    PostEditScreen(
                        mainController = mainController,
                        id = id,
                        onOpenImage = onOpenImage,
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
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    ReportScreen(
                        mainController = mainController,
                        objType = type,
                        id = id,
                    )
                }
            }

            composable(
                route = "message",
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .navigationBarsPadding()
                ) {
                    MessageScreen(mainController = mainController)
                }
            }
        }
    }

    val checkDetectUpgradeState by vm.checkUpdateStateLiveData.observeAsState()

    LaunchedEffect(checkDetectUpgradeState) {
        if(checkDetectUpgradeState != null) {
            mainController.snackbar("检查到新版本，可以前往设置界面更新哦")
            vm.checkUpdateStateLiveData.value = null
        }
    }

}