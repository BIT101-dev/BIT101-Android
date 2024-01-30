package cn.bit101.android.features

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.image.ImageHost
import cn.bit101.android.features.common.component.image.rememberImageHostState
import cn.bit101.android.features.common.component.snackbar.SnackbarHost
import cn.bit101.android.features.common.component.snackbar.rememberSnackbarState
import cn.bit101.android.features.common.helper.getAppVersion
import cn.bit101.android.features.common.nav.DURATION_MILLIS
import cn.bit101.android.features.common.nav.NavAnimation
import cn.bit101.android.features.common.nav.NavDestConfig
import cn.bit101.android.features.common.nav.composableEdit
import cn.bit101.android.features.common.nav.composableIndex
import cn.bit101.android.features.common.nav.composableLogin
import cn.bit101.android.features.common.nav.composableMessage
import cn.bit101.android.features.common.nav.composablePost
import cn.bit101.android.features.common.nav.composablePoster
import cn.bit101.android.features.common.nav.composableReport
import cn.bit101.android.features.common.nav.composableSetting
import cn.bit101.android.features.common.nav.composableUser
import cn.bit101.android.features.common.nav.composableWeb
import cn.bit101.android.features.common.nav.delayRemainTransition
import cn.bit101.android.features.common.nav.enterTransition
import cn.bit101.android.features.common.nav.exitTransition
import cn.bit101.android.features.common.utils.ColorUtils
import cn.bit101.android.features.component.SystemUIConfig
import cn.bit101.android.features.component.WithSystemUIConfig
import cn.bit101.android.features.index.IndexScreen
import cn.bit101.android.features.login.LoginOrLogoutScreen
import cn.bit101.android.features.message.MessageScreen
import cn.bit101.android.features.postedit.PostEditScreen
import cn.bit101.android.features.poster.PosterScreen
import cn.bit101.android.features.report.ReportScreen
import cn.bit101.android.features.setting.SettingScreen
import cn.bit101.android.features.user.UserScreen
import cn.bit101.android.features.versions.UpdateDialog
import cn.bit101.android.features.versions.VersionDialog
import cn.bit101.android.features.web.WebScreen

/**
 * 获取系统 UI 配置，包括状态栏颜色和图标颜色、底部导航栏颜色
 */
@Composable
private fun getSystemUI(destConfig: NavDestConfig?): SystemUIConfig {

    val statusBarColor = when(destConfig) {
        NavDestConfig.Web, NavDestConfig.Message -> Color(0xFFFF9A57)
        NavDestConfig.Setting -> Color.Transparent
        NavDestConfig.User -> Color.Transparent
        NavDestConfig.Post, NavDestConfig.Edit -> Color.Transparent
        NavDestConfig.Report -> Color.Transparent
        NavDestConfig.Poster -> Color.Transparent
        else -> MaterialTheme.colorScheme.background
    }

    val statusBarDarkIcon = when(statusBarColor) {
        Color.Transparent -> ColorUtils.isLightColor(MaterialTheme.colorScheme.background)
        else -> ColorUtils.isLightColor(statusBarColor)
    }

    val navBarColor = if(destConfig == NavDestConfig.Index) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
    } else when(destConfig) {
        NavDestConfig.Post, NavDestConfig.Edit -> MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        NavDestConfig.Report -> MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
        else -> MaterialTheme.colorScheme.background
    }
    return SystemUIConfig(statusBarColor, statusBarDarkIcon, navBarColor)
}

@Composable
internal fun MainApp() {
    val vm: MainViewModel = hiltViewModel()
    val ctx = LocalContext.current

    // 控制器
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
    val currentDestConfig by mainController.currentDestConfigAsState()

    // 状态栏颜色
    val systemUIConfig = getSystemUI(currentDestConfig)
    WithSystemUIConfig(systemUIConfig) {}

    // 版本信息
    val lastVersion = vm.lastVersionFlow.collectAsState(initial = null).value ?: return
    val appVersion = getAppVersion(ctx)

    // 显示当前版本信息
    if(lastVersion < appVersion.versionNumber) {
        VersionDialog(
            onConfirm = vm::logout,
            onDismiss = { vm.setLastVersion(appVersion.versionNumber) }
        )
    }

    // 自动检测更新
    val autoDetectUpgrade by vm.autoDetectUpgradeFlow.collectAsState(initial = false)
    if (autoDetectUpgrade) {
        UpdateDialog()
    }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = NavDestConfig.Index.route,
    ) {
        composableIndex(navController = navController) {
            IndexScreen(mainController)
        }

        composableLogin(navController = navController) {
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                LoginOrLogoutScreen(mainController)
            }
        }

        composableWeb(navAnim, navController) { _, url ->
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .statusBarsPadding()
                ) {
                    WebScreen(mainController, url = url)
                }
            }
        }

        composableSetting(navAnim, navController) { _, initialRoute ->
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                SettingScreen(mainController, initialRoute)
            }
        }

        composableUser(navAnim, navController) { _, uid ->
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    UserScreen(mainController, uid)
                }
            }
        }

        composablePoster(navAnim, navController) { _, id ->
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    PosterScreen(mainController, id)
                }
            }
        }

        composablePost(navAnim, navController) {
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    PostEditScreen(mainController)
                }
            }
        }

        composableEdit(navAnim, navController) { _, id ->
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    PostEditScreen(mainController, id)
                }
            }
        }

        composableReport(navAnim, navController) { _, type, id ->
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    ReportScreen(mainController, type, id,)
                }
            }
        }

        composableMessage(navAnim, navController) {
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                MessageScreen(mainController)
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

/**
 * 导航过渡动画
 */
private val navAnim = NavAnimation(
    enterTransition = { enterTransition },
    exitTransition = { delayRemainTransition },
    popEnterTransition = { EnterTransition.None },
    popExitTransition = { exitTransition },
)