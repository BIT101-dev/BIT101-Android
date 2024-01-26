package cn.bit101.android.config.setting

import cn.bit101.android.config.common.Transformer
import cn.bit101.android.config.common.map
import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.PageSettings
import cn.bit101.android.config.setting.base.PageShowOnNav
import cn.bit101.android.config.setting.base.toPageData
import javax.inject.Inject

internal class DefaultPageSettings @Inject constructor(
    settingDataStore: SettingDataStore
) : PageSettings {

    /**
     * 填充 [PageShowOnNav] 列表，使其包含所有页面
     */
    private fun List<PageShowOnNav>.fill(): List<PageShowOnNav> =
        this + PageShowOnNav.allPages.filter { it !in this }


    /**
     * 隐藏页面的转换器
     */
    private val hidePagesTransformer = object : Transformer<String, List<PageShowOnNav>> {
        override fun invokeTo(value: String): List<PageShowOnNav> {
            // 读取合法且不重复的页面
            return value.split(",")
                .mapNotNull { PageShowOnNav.getPage(it) }
                .distinct()
        }

        override fun invokeFrom(value: List<PageShowOnNav>): String {
            return value.joinToString(",") { it.toPageData().value }
        }
    }

    /**
     * 主页的转换器
     */
    private val homePageTransformer = object : Transformer<String, PageShowOnNav> {
        override fun invokeTo(value: String): PageShowOnNav {
            // 读取合法的页面，如果没有合法的页面，则返回首页
            return PageShowOnNav.getPage(value) ?: PageShowOnNav.homePage
        }

        override fun invokeFrom(value: PageShowOnNav): String {
            return value.toPageData().value
        }
    }

    /**
     * 所有页面顺序的转换器
     */
    private val allPagesTransformer = object : Transformer<String, List<PageShowOnNav>> {
        override fun invokeTo(value: String): List<PageShowOnNav> {
            // 读取合法且不重复的页面，并填充所有页面
            return value.split(",")
                .mapNotNull { PageShowOnNav.getPage(it) }
                .fill()
                .distinct()
        }

        override fun invokeFrom(value: List<PageShowOnNav>): String {
            return value.joinToString(",") { it.toPageData().value }
        }
    }

    override val hiddenPages = settingDataStore.settingPageVisible.toSettingItem().map(hidePagesTransformer)

    override val homePage = settingDataStore.settingHomePage.toSettingItem().map(homePageTransformer)

    override val allPages = settingDataStore.settingPageOrder.toSettingItem().map(allPagesTransformer)
}