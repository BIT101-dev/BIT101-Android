package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.PageSettings
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.ui.common.withScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PageViewModel @Inject constructor(
    private val pageSettings: PageSettings
) : ViewModel() {

    val homePageFlow = pageSettings.homePage.flow
    val allPagesFlow = pageSettings.allPages.flow
    val hiddenPagesFlow = pageSettings.hidePages.flow

    fun changePageSettings(
        allPages: List<PageShowOnNav>,
        homePage: PageShowOnNav,
        hiddenPages: List<PageShowOnNav>
    ) = withScope {
       pageSettings.allPages.set(allPages)
       pageSettings.homePage.set(homePage)
       pageSettings.hidePages.set(hiddenPages)
    }

    fun reset() = withScope {
        pageSettings.allPages.set(PageShowOnNav.allPages)
        pageSettings.homePage.set(PageShowOnNav.homePage)
        pageSettings.hidePages.set(emptyList())
    }
}