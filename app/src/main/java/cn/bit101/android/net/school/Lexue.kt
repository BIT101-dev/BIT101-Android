package cn.bit101.android.net.school

import android.util.Log
import cn.bit101.android.net.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.property.Categories
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Uid
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * @author flwfdd
 * @date 11/05/2023 21:49
 * @description 乐学相关接口
 * _(:з」∠)_
 */

// 获取日程订阅链接
suspend fun getCalendarUrl(): String? {
    return try {
        withContext(Dispatchers.IO) {
            val client = HttpClient.client
            val request = Request.Builder()
                .url(lexueMainUrl)
                .build()
            var sesskey: String? = null
            client.newCall(request).execute().use { response ->
                val html =
                    response.body?.string() ?: throw Exception("get lexue main page error")
                // 正则匹配获取sesskey
                val regex = Regex("[\"|']sesskey[\"|']:[\"|']([^\"|']+?)[\"|']")
                val matchResult = regex.find(html)
                sesskey = matchResult?.groupValues?.get(1)
            }
            if (sesskey == null) {
                throw Exception("get sesskey error")
            }

            val body = FormBody.Builder()
                .add("sesskey", sesskey!!)
                .add("_qf__core_calendar_export_form", "1")
                .add("events[exportevents]", "all")
                .add("period[timeperiod]", "recentupcoming")
                .add("generateurl", "获取日历网址")
                .build()
            val request2 = Request.Builder()
                .url(lexueCalendarExportUrl)
                .post(body)
                .build()
            client.newCall(request2).execute().use { response ->
                val html =
                    response.body?.string() ?: throw Exception("get lexue main page error")
                val doc = Jsoup.parse(html)
                // 通过class查找日历订阅链接
                val div = doc.select(".calendarurl").text()
                if (div.isEmpty()) return@withContext null
                return@withContext div.substring(div.indexOf("http"))
            }
        }
    } catch (Exception: Exception) {
        Log.e("Lexue", Exception.toString())
        return null
    }
}

data class CalendarEvent(
    val uid: String,
    val event: String,
    val description: String,
    val course: String,
    val time: LocalDateTime
)

// 获取日程
suspend fun getCalendar(url: String): List<CalendarEvent> {
    return withContext(Dispatchers.IO) {
        val client = HttpClient.client
        val request = Request.Builder()
            .url(url)
            .build()
        val list = mutableListOf<CalendarEvent>()
        client.newCall(request).execute().use { response ->
            val html =
                response.body?.string() ?: throw Exception("get calendar error")
            val cal = CalendarBuilder().build(html.byteInputStream())
            cal.components.forEach {
                val start = it.getProperty<DtStart>(Property.DTSTART)

                list.add(
                    CalendarEvent(
                        it.getProperty<Uid>(Property.UID).value,
                        it.getProperty<Summary>(Property.SUMMARY).value,
                        it.getProperty<Description>(Property.DESCRIPTION).value,
                        it.getProperty<Categories>(Property.CATEGORIES).value,
                        start.date.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDateTime()
                    )
                )
            }
        }
        return@withContext list
    }
}