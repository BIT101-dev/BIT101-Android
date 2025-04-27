package cn.bit101.android.features

import androidx.compose.animation.EnterTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.image.ImageHost
import cn.bit101.android.features.common.component.image.rememberImageHostState
import cn.bit101.android.features.common.component.snackbar.SnackbarHost
import cn.bit101.android.features.common.component.snackbar.rememberSnackbarState
import cn.bit101.android.features.common.helper.MessageUrl
import cn.bit101.android.features.common.helper.getAppVersion
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
import cn.bit101.android.features.index.IndexScreen
import cn.bit101.android.features.login.LoginOrLogoutScreen
import cn.bit101.android.features.postedit.PostEditScreen
import cn.bit101.android.features.poster.ImageDownloader
import cn.bit101.android.features.poster.PosterScreen
import cn.bit101.android.features.report.ReportScreen
import cn.bit101.android.features.setting.SettingScreen
import cn.bit101.android.features.user.UserScreen
import cn.bit101.android.features.versions.UpdateDialog
import cn.bit101.android.features.versions.VersionDialog
import cn.bit101.android.features.web.WebScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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

    val systemUiController = rememberSystemUiController()

    val bottomNavBarColor = MaterialTheme.colorScheme.surface
    val darkTheme = !ColorUtils.isLightColor(MaterialTheme.colorScheme.background)
    LaunchedEffect(bottomNavBarColor, darkTheme) {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )

        systemUiController.setNavigationBarColor(
            color = bottomNavBarColor
        )
    }

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

    // 图片保存工具类
    val imageDownloader = remember { ImageDownloader() }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = NavDestConfig.Index.route,
    ) {
        composableIndex(navController = navController) {
            IndexScreen(mainController)
        }

        composableLogin(navController = navController) {
            LoginOrLogoutScreen(mainController)
        }

        composableWeb(navAnim, navController) { _, url ->
            WebScreen(mainController, url = url)
        }

        composableSetting(navAnim, navController) { _, initialRoute ->
            SettingScreen(mainController, initialRoute)
        }

        composableUser(navAnim, navController) { _, uid ->
            Box(modifier = Modifier.navigationBarsPadding()) {
                UserScreen(mainController, uid)
            }
        }

        composablePoster(navAnim, navController) { _, id ->
            Box(modifier = Modifier.navigationBarsPadding()) {
                PosterScreen(mainController, id)
            }
        }

        composablePost(navAnim, navController) {
            Box(modifier = Modifier.navigationBarsPadding()) {
                PostEditScreen(mainController)
            }
        }

        composableEdit(navAnim, navController) { _, id ->
            Box(modifier = Modifier.navigationBarsPadding()) {
                PostEditScreen(mainController, id)
            }
        }

        composableReport(navAnim, navController) { _, type, id ->
            Box(modifier = Modifier.navigationBarsPadding()) {
                ReportScreen(mainController, type, id,)
            }
        }

        composableMessage(navAnim, navController) {
            Box(modifier = Modifier.navigationBarsPadding()) {
                WebScreen(mainController, MessageUrl)
            }
        }
    }


    ImageHost(
        modifier = Modifier.fillMaxSize(),
        state = mainController.imageHostState,
        onDownloadImage = { str: String, callback: () -> Unit ->
            imageDownloader.downloadAndAddImage(str, ctx, mainController, callback)
        },
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