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
    private fun List<PageShowOnNav>.fill(): List<PageShowOnNav> =
        this + PageShowOnNav.allPages.filter { it !in this }


    private val hidePagesTransformer = object : Transformer<String, List<PageShowOnNav>> {
        override fun invokeTo(value: String): List<PageShowOnNav> {
            return value.split(",")
                .mapNotNull { PageShowOnNav.getPage(it) }
                .distinct()
        }

        override fun invokeFrom(value: List<PageShowOnNav>): String {
            return value.joinToString(",") { it.toPageData().value }
        }
    }

    private val homePageTransformer = object : Transformer<String, PageShowOnNav> {
        override fun invokeTo(value: String): PageShowOnNav {
            return PageShowOnNav.getPage(value) ?: PageShowOnNav.homePage
        }

        override fun invokeFrom(value: PageShowOnNav): String {
            return value.toPageData().value
        }
    }

    private val allPagesTransformer = object : Transformer<String, List<PageShowOnNav>> {
        override fun invokeTo(value: String): List<PageShowOnNav> {
            return value.split(",")
                .mapNotNull { PageShowOnNav.getPage(it) }
                .fill()
                .distinct()
        }

        override fun invokeFrom(value: List<PageShowOnNav>): String {
            return value.joinToString(",") { it.toPageData().value }
        }
    }

    override val hidePages = settingDataStore.settingPageVisible.toSettingItem().map(hidePagesTransformer)

    override val homePage = settingDataStore.settingHomePage.toSettingItem().map(homePageTransformer)

    override val allPages = settingDataStore.settingPageOrder.toSettingItem().map(allPagesTransformer)
}