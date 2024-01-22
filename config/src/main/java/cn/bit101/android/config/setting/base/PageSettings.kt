package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem

/**
 * 显示在底部导航栏上的页面
 */
sealed interface PageShowOnNav {

    /**
     * 卷
     */
    object Schedule : PageShowOnNav

    /**
     * 图
     */
    object Map : PageShowOnNav

    /**
     * 网
     */
    object BIT101Web : PageShowOnNav

    /**
     * 话
     */
    object Gallery : PageShowOnNav

    /**
     * 我
     */
    object Mine : PageShowOnNav

    companion object {

        /**
         * 所有页面
         */
        val allPages = listOf(Schedule, Map, BIT101Web, Gallery, Mine)

        /**
         * 首页
         */
        val homePage = allPages[0]

        /**
         * 通过字符串获取页面
         */
        fun getPage(pageStr: String) =
            allPages.firstOrNull { it.toPageData().route == pageStr }
    }
}

/**
 * 页面数据，包含页面名字和页面的 route
 */
data class PageData(
    /**
     * 页面名字
     */
    val name: String,

    /**
     * 页面的 route
     */
    val route: String
)

/**
 * 将 [PageShowOnNav] 转换为 [PageData]
 */
fun PageShowOnNav.toPageData() = when (this) {
    PageShowOnNav.Schedule -> PageData("卷", "schedule")
    PageShowOnNav.Map -> PageData("图", "map")
    PageShowOnNav.BIT101Web -> PageData("网", "bit101-web")
    PageShowOnNav.Gallery -> PageData("话", "gallery")
    PageShowOnNav.Mine -> PageData("我", "mine")
}

interface PageSettings {

    /**
     * 首页
     */
    val homePage: SettingItem<PageShowOnNav>

    /**
     * 隐藏的页面
     */
    val hidePages: SettingItem<List<PageShowOnNav>>

    /**
     * 所有页面的顺序
     */
    val allPages: SettingItem<List<PageShowOnNav>>
}