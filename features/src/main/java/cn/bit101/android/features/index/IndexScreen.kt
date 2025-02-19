package cn.bit101.android.features.index

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.motion.materialSharedAxisZIn
import cn.bit101.android.features.common.motion.materialSharedAxisZOut
import cn.bit101.android.features.component.WithLoginStatus
import cn.bit101.android.features.gallery.GalleryScreen
import cn.bit101.android.features.map.MapScreen
import cn.bit101.android.features.mine.MineScreen
import cn.bit101.android.features.schedule.ScheduleScreen
import cn.bit101.android.features.web.WebScreen


/**
 * 主页底部导航栏
 */
@Composable
private fun IndexScreenNavBar(
    pages: List<IndexPage>,
    selectedIndex: String,
    onSelected: (String) -> Unit,
) {
    NavigationBar {
        pages.forEach { page ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = page.label
                    )
                },
                label = { Text(text = page.label) },
                selected = page.route == selectedIndex,
                onClick = { onSelected(page.route) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun IndexScreen(
    mainController: MainController,
) {
    val vm: IndexViewModel = hiltViewModel()
    val indexScreenConfig = vm.indexScreenConfigFlow.collectAsState(initial = null).value ?: return

    val loginStatus by vm.loginStatusFlow.collectAsState(initial = null)

    val state = rememberPagerState(
        initialPage = indexScreenConfig.initialPage,
        pageCount = { indexScreenConfig.pages.size }
    )

    val navController = rememberNavController()

    val startRoute = PageShowOnNav.Schedule.toString()
    val navEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navEntry?.destination?.route ?: startRoute

    LaunchedEffect(indexScreenConfig) {
        if (state.currentPage >= indexScreenConfig.pages.size) {
            state.scrollToPage(indexScreenConfig.initialPage)
        }
    }

    Scaffold(
        bottomBar = {
            IndexScreenNavBar(
                pages = indexScreenConfig.pages,
                selectedIndex = currentRoute,
                onSelected = {
                    if (it == currentRoute) return@IndexScreenNavBar
                    navController.navigate(route = it)
                }
            )
        }
    ) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()

        NavHost (
            modifier = Modifier.padding(bottom = bottomPadding),
            navController = navController,
            startDestination = startRoute,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition },
        ) {
            composable(route = PageShowOnNav.BIT101Web.toString()) {
                WebScreen(mainController)
            }
            composable(route = PageShowOnNav.Gallery.toString()) {
                WithLoginStatus(mainController, loginStatus) {
                    GalleryScreen(mainController)
                }
            }
            composable(route = PageShowOnNav.Map.toString()) {
                MapScreen()
            }
            composable(route = PageShowOnNav.Mine.toString()) {
                MineScreen(mainController)
            }
            composable(route = PageShowOnNav.Schedule.toString()) {
                WithLoginStatus(mainController, loginStatus) {
                    ScheduleScreen(mainController)
                }
            }
        }
    }

}

private val enterTransition = materialSharedAxisZIn()
private val exitTransition = materialSharedAxisZOut()
private val popEnterTransition = materialSharedAxisZIn()
private val popExitTransition = materialSharedAxisZOut()