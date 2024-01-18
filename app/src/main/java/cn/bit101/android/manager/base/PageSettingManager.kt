package cn.bit101.android.manager.base

import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.api.model.common.NameAndValue

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
            allPages.firstOrNull { it.toNameAndValue().value == pageStr }
    }
}

fun PageShowOnNav.toNameAndValue() = when (this) {
    PageShowOnNav.Schedule -> NameAndValue("卷", "schedule")
    PageShowOnNav.Map -> NameAndValue("图", "map")
    PageShowOnNav.BIT101Web -> NameAndValue("网", "bit101-web")
    PageShowOnNav.Gallery -> NameAndValue("话", "gallery")
    PageShowOnNav.Mine -> NameAndValue("我", "mine")
}

interface PageSettingManager {
    val homePage: SettingItem<PageShowOnNav>
    val hidePages: SettingItem<List<PageShowOnNav>>
    val allPages: SettingItem<List<PageShowOnNav>>
}