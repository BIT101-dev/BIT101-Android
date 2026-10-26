package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.CampusInfo
import cn.bit101.api.model.common.CourseForSchedule
import cn.bit101.api.model.common.ExamInfo
import cn.bit101.api.model.common.SchoolCookie
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.net.URLEncoder
import com.google.gson.Gson

/**
 * 教学中心接口：保留原字段映射，在解析前检查会话与业务错误
 */
class SchoolJxzxehallService internal constructor(
    private val logger: Logger,
    private val sessions: TeachingCenterSession,
) {
    constructor(logger: Logger = emptyLogger) : this(logger, TeachingCenterSession())

    private val gson = Gson()

    suspend fun getCampusInfos(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
    ): List<CampusInfo> = withSession(cookies, webVpn, "获取校区列表", "kxjasbyMobile") { session ->
        session.json("/jwapp/sys/kxjasbyMobile/modules/jxllb/ggzdpx.do?dicCode=48682&SFSY=1&order=%2BDM").rows("ggzdpx").map {
            CampusInfo(displayName = it.asJsonObject.text("MC").orEmpty(), code = it.asJsonObject.text("DM").orEmpty())
        }
    }

    suspend fun getBuildingInfos(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        campusCode: String?,
    ): List<BuildingInfo> = withSession(cookies, webVpn, "获取教学楼列表", "kxjasbyMobile") { session ->
        val query = campusCode?.let { "?XXXQDM=" + URLEncoder.encode(it, "UTF-8") }.orEmpty()
        session.json("/jwapp/sys/kxjasbyMobile/modules/jxllb/cxjxl.do$query").rows("cxjxl").map { row ->
            val it = row.asJsonObject
            BuildingInfo(
                buildingName = it.text("JXLMC").orEmpty(),
                buildingIndex = it.text("JXLDM").orEmpty(),
                campusName = it.text("XXXQDM_DISPLAY").orEmpty(),
                campusCode = it.text("XXXQDM").orEmpty(),
            )
        }
    }

    suspend fun getCurrentTerm(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
    ): String = withSession(cookies, webVpn, "获取当前学期") { session ->
        session.json("/jwapp/sys/wdkbby/modules/jshkcb/dqxnxq.do").rows("dqxnxq")
            .firstOrNull()?.asJsonObject?.text("DM")?.takeIf { it.isNotBlank() }
            ?: throw SchoolResponseError("教学中心未返回当前学期")
    }

    suspend fun getTermList(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
    ): List<String> = withSession(cookies, webVpn, "获取学期列表") { session ->
        session.json("/jwapp/sys/wdkbby/modules/jshkcb/xnxqcx.do").rows("xnxqcx")
            .map { it.asJsonObject.text("DM") ?: throw SchoolResponseError("教学中心学期数据缺少编码") }
    }

    suspend fun getFirstDay(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        term: String,
    ): String = withSession(cookies, webVpn, "获取学期开始日期") { session ->
        val payload = gson.toJson(mapOf("XNXQDM" to term, "ZC" to "1"))
        val data = session.json("/jwapp/sys/wdkbby/wdkbByController/cxzkbrq.do", mapOf("requestParamStr" to payload))
        data.get("data")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.firstOrNull { it.asJsonObject.text("XQ") == "1" }?.asJsonObject?.text("RQ")
            ?: throw Exception("学期 $term 第一周星期一日期缺失")
    }

    suspend fun getExamList(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        term: String,
    ): List<ExamInfo> = withSession(cookies, webVpn, "获取考试安排", "wdksapMobile") { session ->
        session.json("/jwapp/sys/wdksapMobile/modules/ksap/cxxsksap.do",
            mapOf("XNXQDM" to term, "*order" to "-KSRQ")).rows("cxxsksap").map { row ->
            val it = row.asJsonObject
            ExamInfo(
                location = it.text("JASMC"),
                time = it.text("KSSJMS").orEmpty(),
                date = it.text("KSRQ").orEmpty(),
                seatId = it.text("ZWH"),
                ksmc = it.text("KSMC"),
                termCode = it.text("XNXQDM_DISPLAY"),
                courseCode = it.text("KCM"),
                teacherName = it.text("ZJJSXM"),
                kch = it.text("KCH"),
            )
        }
    }

    suspend fun getCourseSchedule(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        term: String,
    ): List<CourseForSchedule> = withSession(cookies, webVpn, "获取课程表") { session ->
        val rows = session.json("/jwapp/sys/wdkbby/modules/xskcb/cxxszhxqkb.do", mapOf("XNXQDM" to term))
            .rows("cxxszhxqkb")
        val courses = try {
            gson.fromJson(rows, Array<CourseForSchedule>::class.java).toList()
        } catch (e: RuntimeException) {
            throw SchoolResponseError("教学中心课程数据格式异常")
        }
        if (courses.any { !it.hasValidPlacement() }) throw SchoolResponseError("教学中心课程安排不完整，请稍后重试")
        courses.map { course ->
            course.copy(XNXQDM = course.XNXQDM?.takeIf(String::isNotBlank) ?: term,
                KCM = course.KCM?.takeIf(String::isNotBlank) ?: course.KCH)
        }
    }

    private fun CourseForSchedule?.hasValidPlacement(): Boolean {
        if (this == null || KCM.isNullOrBlank() && KCH.isNullOrBlank() || XF?.isFinite() == false) return false
        val weeks = SKZC.orEmpty()
        if (weeks.any { it != '0' && it != '1' }) return false
        val day = SKXQ ?: 0
        val start = KSJC ?: 0
        val end = JSJC ?: 0
        // 学校未排时间的课程可以保留；部分缺失或越界的已排课程不得覆盖正常缓存。
        if ('1' !in weeks && day == 0 && start == 0 && end == 0) return true
        return '1' in weeks && day in 1..7 && start > 0 && end in start..1440
    }

    private fun JsonObject.rows(name: String): JsonArray =
        get("datas")?.takeIf { it.isJsonObject }?.asJsonObject
            ?.get(name)?.takeIf { it.isJsonObject }?.asJsonObject
            ?.get("rows")?.takeIf { it.isJsonArray }?.asJsonArray
            ?: throw SchoolResponseError("教学中心未返回预期数据（$name）")

    private suspend fun <T> withSession(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        action: String,
        app: String = "wdkbby",
        block: suspend (TeachingCenterRequests) -> T,
    ): T {
        try {
            return sessions.use(cookies, webVpn, app, block)
        } catch (e: Throwable) {
            logger.err("SchoolJxzxehallService", "$action 失败: ${e.javaClass.name}")
            throw e
        }
    }
}
