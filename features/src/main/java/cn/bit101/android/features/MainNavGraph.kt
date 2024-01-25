package cn.bit101.android.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.createGraph
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.nav.NavDest
import cn.bit101.android.features.common.nav.composableBIT101Web
import cn.bit101.android.features.common.nav.composableEdit
import cn.bit101.android.features.common.nav.composableGallery
import cn.bit101.android.features.common.nav.composableLogin
import cn.bit101.android.features.common.nav.composableMap
import cn.bit101.android.features.common.nav.composableMessage
import cn.bit101.android.features.common.nav.composableMine
import cn.bit101.android.features.common.nav.composablePost
import cn.bit101.android.features.common.nav.composablePoster
import cn.bit101.android.features.common.nav.composableReport
import cn.bit101.android.features.common.nav.composableSchedule
import cn.bit101.android.features.common.nav.composableSetting
import cn.bit101.android.features.common.nav.composableUser
import cn.bit101.android.features.common.nav.composableWeb
import cn.bit101.android.features.gallery.GalleryScreen
import cn.bit101.android.features.login.LoginOrLogoutScreen
import cn.bit101.android.features.map.MapScreen
import cn.bit101.android.features.message.MessageScreen
import cn.bit101.android.features.mine.MineScreen
import cn.bit101.android.features.postedit.PostEditScreen
import cn.bit101.android.features.poster.PosterScreen
import cn.bit101.android.features.report.ReportScreen
import cn.bit101.android.features.schedule.ScheduleScreen
import cn.bit101.android.features.setting.SettingScreen
import cn.bit101.android.features.user.UserScreen
import cn.bit101.android.features.web.WebScreen

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
                    mainController.navigate(NavDest.Login) {
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
internal fun rememberMainNavGraph(
    mainController: MainController,
    navController: NavHostController,
    paddingValues: PaddingValues,
    loginStatus: Boolean?,
    startDestination: String,
    route: String? = null,
): NavGraph {
    return remember(
        mainController,
        paddingValues,
        loginStatus,
        startDestination,
        route
    ) {
        val builder: NavGraphBuilder.() -> Unit = {
            composableSchedule {
                WithLoginStatus(mainController, loginStatus) {
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ScheduleScreen(mainController)
                    }
                }
            }

            composableLogin {
                LoginOrLogoutScreen(mainController)
            }

            composableMap {
                MapScreen()
            }

            composableBIT101Web {
                Box(modifier = Modifier.padding(paddingValues)) {
                    WebScreen(mainController)
                }
            }

            composableWeb { _, url ->
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .statusBarsPadding()
                ) {
                    WebScreen(mainController, url = url)
                }
            }

            composableGallery {
                WithLoginStatus(mainController, loginStatus) {
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        GalleryScreen(mainController)
                    }
                }
            }

            composableMine {
                Box(
                    modifier = Modifier.padding(
                        bottom = paddingValues.calculateBottomPadding()
                    )
                ) {
                    MineScreen(mainController)
                }
            }

            composableSetting { _, initialRoute ->
                SettingScreen(mainController, initialRoute)
            }

            composableUser { _, uid ->
                Box(modifier = Modifier.navigationBarsPadding()) {
                    UserScreen(mainController, uid)
                }
            }

            composablePoster { _, id ->
                Box(modifier = Modifier.navigationBarsPadding()) {
                    PosterScreen(mainController, id)
                }
            }

            composablePost {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    PostEditScreen(mainController)
                }
            }

            composableEdit { _, id ->
                Box(modifier = Modifier.navigationBarsPadding()) {
                    PostEditScreen(mainController, id)
                }
            }

            composableReport { _, type, id ->
                Box(modifier = Modifier.navigationBarsPadding()) {
                    ReportScreen(mainController, type, id,)
                }
            }

            composableMessage {
                MessageScreen(mainController)
            }

        }


        navController.createGraph(
            startDestination = startDestination,
            route = route,
            builder = builder
        )
    }
}