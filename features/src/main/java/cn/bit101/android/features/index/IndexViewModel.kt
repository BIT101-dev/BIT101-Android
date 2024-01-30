package cn.bit101.android.features.index

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.PageSettings
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.config.setting.base.toPageData
import cn.bit101.android.config.user.base.LoginStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal data class IndexPage(
    val page: PageShowOnNav,
    val label: String,
    val icon: ImageVector,
)

internal data class IndexScreenConfig(
    val pages: List<IndexPage>,
    val initialPage: Int,
)

@HiltViewModel
internal class IndexViewModel @Inject constructor(
    private val pageSettings: PageSettings,
    private val loginStatus: LoginStatus
) : ViewModel() {
    val indexScreenConfigFlow = combine(
        pageSettings.homePage.flow,
        pageSettings.hiddenPages.flow,
        pageSettings.allPages.flow
    ) { homePage, hiddenPages, allPages ->
        val pages = allPages.filter { it !in hiddenPages }

        val indexPages = allPages
            .filter { it !in hiddenPages }
            .map {
                val label = it.toPageData().name
                val icon = when(it) {
                    PageShowOnNav.Schedule -> Icons.Rounded.Event
                    PageShowOnNav.Map -> Icons.Rounded.Map
                    PageShowOnNav.BIT101Web -> Icons.Rounded.Explore
                    PageShowOnNav.Gallery -> Icons.AutoMirrored.Rounded.Chat
                    PageShowOnNav.Mine -> Icons.Rounded.AccountCircle
                }
                IndexPage(it, label, icon)
            }

        val initialPage = indexPages.indexOfFirst { it.page == homePage }

        IndexScreenConfig(
            pages = indexPages,
            initialPage = initialPage,
        )
    }

    val loginStatusFlow = loginStatus.status.flow
}