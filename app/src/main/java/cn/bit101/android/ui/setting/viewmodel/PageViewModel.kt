package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.manager.base.PageSettingManager
import cn.bit101.android.manager.base.PageShowOnNav
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PageViewModel @Inject constructor(
    private val pageSettingManager: PageSettingManager
) : ViewModel() {

    val homePageFlow = pageSettingManager.homePage.flow
    val allPagesFlow = pageSettingManager.allPages.flow
    val hiddenPagesFlow = pageSettingManager.hidePages.flow

    fun changePageSettings(
        allPages: List<PageShowOnNav>,
        homePage: PageShowOnNav,
        hiddenPages: List<PageShowOnNav>
    ) {
       viewModelScope.launch(Dispatchers.IO) {
           pageSettingManager.allPages.set(allPages)
           pageSettingManager.homePage.set(homePage)
           pageSettingManager.hidePages.set(hiddenPages)
       }
    }

    fun reset() {
        viewModelScope.launch(Dispatchers.IO) {
            pageSettingManager.allPages.set(PageShowOnNav.allPages)
            pageSettingManager.homePage.set(PageShowOnNav.homePage)
            pageSettingManager.hidePages.set(emptyList())
        }
    }
}