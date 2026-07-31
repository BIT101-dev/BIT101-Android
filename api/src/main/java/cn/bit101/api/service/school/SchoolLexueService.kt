package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.api.model.http.school.GetCalendarDataModel
import cn.bit101.bitlogin.api.lexue.LexueCalendar
import cn.bit101.bitlogin.http.HttpClient

/**
 * 基于 BIT-Login SDK 的乐学 (Moodle) 服务
 */
class SchoolLexueService(
    private val logger: Logger = emptyLogger,
) {
    suspend fun getCalendarUrl(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
    ): String = withSession(cookies, webVpn, "获取日历订阅链接") { session ->
        LexueCalendar(session).getCalendarUrl()
    }

    suspend fun getCalendar(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        url: String,
    ): List<GetCalendarDataModel.CalendarEvent> = withSession(cookies, webVpn, "获取日历") { session ->
        LexueCalendar(session).getCalendar(url).map {
            GetCalendarDataModel.CalendarEvent(
                uid = it.uid,
                event = it.event,
                description = it.description,
                course = it.course,
                time = it.time,
            )
        }
    }

    private suspend fun <T> withSession(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        action: String,
        block: suspend (HttpClient) -> T,
    ): T {
        val session = openSchoolSession(cookies, webVpn)
        try {
            return block(session)
        } catch (e: Throwable) {
            logger.err("SchoolLexueService", "$action 失败: ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }
}
