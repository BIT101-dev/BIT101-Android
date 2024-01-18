package cn.bit101.android.manager

import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.manager.base.PageSettingManager
import cn.bit101.android.manager.base.PageShowOnNav
import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.android.manager.basic.Transformer
import cn.bit101.android.manager.basic.map
import cn.bit101.android.manager.base.toNameAndValue
import cn.bit101.android.manager.basic.toSettingItem
import javax.inject.Inject

class DefaultPageSettingManager @Inject constructor(
    private val settingDataStore: SettingDataStore
) : PageSettingManager {

    private fun List<PageShowOnNav>.fill(): List<PageShowOnNav> =
        this + PageShowOnNav.allPages.filter { it !in this }


    private val hidePagesTransformer = object : Transformer<String, List<PageShowOnNav>> {
        override fun invokeTo(value: String): List<PageShowOnNav> {
            return value.split(",")
                .mapNotNull { PageShowOnNav.getPage(it) }
                .distinct()
        }

        override fun invokeFrom(value: List<PageShowOnNav>): String {
            return value.joinToString(",") { it.toNameAndValue().value }
        }
    }

    private val homePageTransformer = object : Transformer<String, PageShowOnNav> {
        override fun invokeTo(value: String): PageShowOnNav {
            return PageShowOnNav.getPage(value) ?: PageShowOnNav.homePage
        }

        override fun invokeFrom(value: PageShowOnNav): String {
            return value.toNameAndValue().value
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
            return value.joinToString(",") { it.toNameAndValue().value }
        }
    }

    override val hidePages = settingDataStore.settingPageVisible.toSettingItem().map(hidePagesTransformer)

    override val homePage = settingDataStore.settingHomePage.toSettingItem().map(homePageTransformer)

    override val allPages = settingDataStore.settingPageOrder.toSettingItem().map(allPagesTransformer)
}