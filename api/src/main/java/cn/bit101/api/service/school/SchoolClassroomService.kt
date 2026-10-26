package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.ClassroomInfo
import cn.bit101.api.model.common.SchoolCookie
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.time.LocalDate

/** 空教室查询与课表共用会话校验、应用预热及路线错误分类。 */
class SchoolClassroomService internal constructor(
    private val logger: Logger,
    private val sessions: TeachingCenterSession,
) {
    constructor(logger: Logger = emptyLogger) : this(logger, TeachingCenterSession())

    suspend fun getClassroomOccupancy(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        buildingCode: String,
        dateStr: String = LocalDate.now().toString(),
    ): List<ClassroomInfo> = try {
        sessions.use(cookies, webVpn, "kxjas") { session ->
            val day = queryDay(session, cookies, webVpn, dateStr)
            val query = Gson().toJson(listOf(mapOf(
                "name" to "JXLDM", "caption" to "教学楼代码", "builder" to "equal",
                "linkOpt" to "AND", "value" to buildingCode,
            )))
            val form = day + mapOf(
                "querySetting" to query, "*order" to "+JASMC", "pageSize" to "50",
            )
            val rooms = mutableListOf<ClassroomInfo>()
            // 限制异常分页响应，避免接口重复返回同一页时无限请求。
            for (page in 1..100) {
                val data = session.query("cxjsqk", form + ("pageNumber" to page.toString()))
                val rows = data.get("rows")?.takeIf { it.isJsonArray }?.asJsonArray
                    ?: throw SchoolResponseError("教学中心未返回教室列表")
                rooms += rows.map { row ->
                    val room = row.asJsonObject
                    val busy = (1..13).filter { section ->
                        room.text("JC$section").orEmpty().split(',').any { it.startsWith("1_") }
                    }
                    ClassroomInfo(
                        classroomName = room.text("JASMC").orEmpty(),
                        busyTimeStr = busy.takeIf { it.isNotEmpty() }?.joinToString(","),
                    )
                }
                val total = data.text("totalSize")?.toIntOrNull()
                if (total != null && rooms.size >= total || total == null && rows.size() < 50) return@use rooms
                if (rows.size() == 0) throw SchoolResponseError("教学中心教室分页数据不完整")
            }
            throw SchoolResponseError("教学中心返回的教室分页过多，请稍后重试")
        }
    } catch (e: Exception) {
        logger.err("SchoolClassroomService", "查询空教室失败: ${e.message}")
        throw e
    }

    private data class DayQuery(
        val date: String,
        val webVpn: Boolean,
        val credentials: List<SchoolCookie>,
        val form: Map<String, String>,
    )
    private var dayQuery: DayQuery? = null

    // 在共享会话的互斥锁内访问，每个日期/会话只获取一次校历，不依赖课表页选中的学期。
    private suspend fun queryDay(
        session: TeachingCenterRequests,
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        date: String,
    ): Map<String, String> {
        LocalDate.parse(date)
        val credentials = cookies.map { it.copy(expiresEpochSeconds = null) }
        dayQuery?.takeIf { it.date == date && it.webVpn == webVpn && it.credentials == credentials }
            ?.let { return it.form }
        val current = session.query("dqxnxqcx").get("rows")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.firstOrNull()?.asJsonObject ?: throw SchoolResponseError("教学中心未返回当前学期")
        val term = current.text("DM") ?: throw SchoolResponseError("学期信息缺失")
        val week = session.query("rqzhzcjc", mapOf(
            "RQ" to date,
            "XN" to (current.text("XNDM") ?: throw SchoolResponseError("学年信息缺失")),
            "XQ" to (current.text("XQDM") ?: throw SchoolResponseError("学期信息缺失")),
        ))
        val number = week.text("ZC")?.toIntOrNull()
            ?: throw SchoolResponseError("教学中心未返回查询日期对应的教学周")
        val day = week.text("XQJ")?.toIntOrNull()?.takeIf { it in 1..7 }
            ?: throw SchoolResponseError("教学中心未返回查询日期对应的教学星期")
        val form = mapOf("XNXQDM" to term, "ZC" to number.toString(), "XQ" to day.toString(), "RQ" to date)
        dayQuery = DayQuery(date, webVpn, credentials, form)
        return form
    }

    private suspend fun TeachingCenterRequests.query(name: String, form: Map<String, String> = emptyMap()): JsonObject =
        json("/jwapp/sys/kxjas/modules/kxjas/$name.do", form)
            .getAsJsonObject("datas")?.getAsJsonObject(name)
            ?: throw SchoolResponseError("教学中心未返回预期数据（$name）")
}
