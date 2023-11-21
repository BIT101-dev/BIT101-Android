package cn.bit101.android.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Settings
import cn.bit101.api.model.common.NameAndValue

object PageUtils {

    private val allPages = listOf(
        NameAndValue("卷", "schedule"),
        NameAndValue("图", "map"),
        NameAndValue("网", "bit101-web"),
        NameAndValue("话", "gallery"),
        NameAndValue("我", "mine"),
    )

    val iconsMap = mapOf(
        "schedule" to Icons.Rounded.Event,
        "map" to Icons.Rounded.Map,
        "bit101-web" to Icons.Rounded.Explore,
        "gallery" to Icons.AutoMirrored.Rounded.Chat,
        "mine" to Icons.Rounded.AccountCircle,
    )

    fun getReorderedPages(pagesStr: String): List<NameAndValue<String>> {
        val pages = pagesStr.split(",").filter { page ->
            page.isNotEmpty() && page in allPages.map { it.value }
        }.mapNotNull { value ->
            allPages.firstOrNull { it.value == value }
        }.toMutableList()

        allPages.forEach {
            if(it !in pages) {
                pages.add(it)
            }
        }
        return pages
    }

    fun getPages(pagesStr: String): List<NameAndValue<String>> {
        return pagesStr.split(",").filter { page ->
            page.isNotEmpty() && page in allPages.map { it.value }
        }.mapNotNull { value ->
            allPages.firstOrNull { it.value == value }
        }.toMutableList()
    }

    fun getPage(pageStr: String) =
        allPages.firstOrNull { it.value == pageStr } ?: allPages.first()
}