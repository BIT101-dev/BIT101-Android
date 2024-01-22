package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

sealed interface PageShowOnNav {
    object Schedule : PageShowOnNav
    object Map : PageShowOnNav
    object BIT101Web : PageShowOnNav
    object Gallery : PageShowOnNav
    object Mine : PageShowOnNav

    companion object {
        val allPages = listOf(Schedule, Map, BIT101Web, Gallery, Mine)
        val homePage = allPages[0]
        fun getPage(pageStr: String) =
            allPages.firstOrNull { it.toPageData().value == pageStr }
    }
}

data class PageData(
    val name: String,
    val value: String
)

fun PageShowOnNav.toPageData() = when (this) {
    PageShowOnNav.Schedule -> PageData("卷", "schedule")
    PageShowOnNav.Map -> PageData("图", "map")
    PageShowOnNav.BIT101Web -> PageData("网", "bit101-web")
    PageShowOnNav.Gallery -> PageData("话", "gallery")
    PageShowOnNav.Mine -> PageData("我", "mine")
}

interface PageSettings {
    val homePage: SettingItem<PageShowOnNav>
    val hidePages: SettingItem<List<PageShowOnNav>>
    val allPages: SettingItem<List<PageShowOnNav>>
}