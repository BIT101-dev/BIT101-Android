package cn.bit101.android.features.index

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.nav.NavDestConfig
import cn.bit101.android.features.common.utils.ColorUtils
import cn.bit101.android.features.component.SystemUIConfig
import cn.bit101.android.features.component.WithLoginStatus
import cn.bit101.android.features.component.WithSystemUIConfig
import cn.bit101.android.features.gallery.GalleryScreen
import cn.bit101.android.features.map.MapScreen
import cn.bit101.android.features.mine.MineScreen
import cn.bit101.android.features.schedule.ScheduleScreen
import cn.bit101.android.features.web.WebScreen
import kotlinx.coroutines.launch


/**
 * 主页底部导航栏
 */
@Composable
private fun IndexScreenNavBar(
    pages: List<IndexPage>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    NavigationBar {
        pages.forEachIndexed { i, page ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = page.label
                    )
                },
                label = { Text(text = page.label) },
                selected = i == selectedIndex,
                onClick = { onSelected(i) }
            )
        }
    }
}

/**
 * 获取系统 UI 配置，包括状态栏颜色和图标颜色、底部导航栏颜色
 */
@Composable
private fun getSystemUI(page: PageShowOnNav): SystemUIConfig {

    val statusBarColor = when(page) {
        PageShowOnNav.BIT101Web -> Color(0xFFFF9A57)
        else -> Color.Transparent
    }

    val statusBarDarkIcon = when(statusBarColor) {
        Color.Transparent -> ColorUtils.isLightColor(MaterialTheme.colorScheme.background)
        else -> ColorUtils.isLightColor(statusBarColor)
    }

    val navBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)

    return SystemUIConfig(statusBarColor, statusBarDarkIcon, navBarColor)
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

    val scope = rememberCoroutineScope()

    LaunchedEffect(indexScreenConfig) {
        state.scrollToPage(indexScreenConfig.initialPage)
    }

    Scaffold(
        bottomBar = {
            IndexScreenNavBar(
                pages = indexScreenConfig.pages,
                selectedIndex = state.currentPage,
                onSelected = {
                    scope.launch { state.scrollToPage(it) }
                }
            )
        }
    ) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()

        HorizontalPager(
            modifier = Modifier.padding(bottom = bottomPadding),
            userScrollEnabled = false,
            state = state
        ) {
            if(it >= indexScreenConfig.pages.size) {
                return@HorizontalPager
            }
            val page = indexScreenConfig.pages[it].page

            val systemUIConfig = getSystemUI(page)
            WithSystemUIConfig(systemUIConfig = systemUIConfig) {
                when (page) {
                    PageShowOnNav.BIT101Web -> @Composable {
                        WebScreen(mainController)
                    }

                    PageShowOnNav.Gallery -> @Composable {
                        WithLoginStatus(mainController, loginStatus) {
                            GalleryScreen(mainController)
                        }
                    }

                    PageShowOnNav.Map -> @Composable {
                        MapScreen()
                    }

                    PageShowOnNav.Mine -> @Composable {
                        MineScreen(mainController)
                    }

                    PageShowOnNav.Schedule -> @Composable {
                        WithLoginStatus(mainController, loginStatus) {
                            ScheduleScreen(mainController)
                        }
                    }
                }
            }
        }


    }

}