package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.CampusInfo
import cn.bit101.api.model.common.CourseForSchedule
import cn.bit101.api.model.common.ExamInfo
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.Config
import cn.bit101.bitlogin.api.jxzxehall.Campus
import cn.bit101.bitlogin.api.jxzxehall.Course
import cn.bit101.bitlogin.api.jxzxehall.Exam
import cn.bit101.bitlogin.api.jxzxehall.Term
import cn.bit101.bitlogin.http.HttpClient
import com.google.gson.Gson

/**
 * 基于 BIT-Login SDK 的教学中心 (一站式大厅) 服务
 */
class SchoolJxzxehallService(
    private val logger: Logger = emptyLogger,
) {
    private val gson = Gson()

    suspend fun getCampusInfos(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
    ): List<CampusInfo> = withSession(cookies, webVpn, "获取校区列表") { session ->
        Campus(session).getCampusList().map {
            CampusInfo(displayName = it["name"].orEmpty(), code = it["code"].orEmpty())
        }
    }

    suspend fun getBuildingInfos(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        campusCode: String?,
    ): List<BuildingInfo> = withSession(cookies, webVpn, "获取教学楼列表") { session ->
        Campus(session).getBuildingList(campusCode).map {
            BuildingInfo(
                buildingName = it["name"].orEmpty(),
                buildingIndex = it["code"].orEmpty(),
                campusName = it["campus_name"].orEmpty(),
                campusCode = it["campus_code"].orEmpty(),
            )
        }
    }

    suspend fun getCurrentTerm(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
    ): String = withSession(cookies, webVpn, "获取当前学期") { session ->
        Term(session).getCurrentTerm()
    }

    suspend fun getTermList(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
    ): List<String> = withSession(cookies, webVpn, "获取学期列表") { session ->
        Term(session).getTermList()
    }

    suspend fun getFirstDay(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        term: String,
    ): String = withSession(cookies, webVpn, "获取学期开始日期") { session ->
        Term(session).getWeekAndDate(term, week = 1)
            .firstOrNull { it["dayOfWeek"] == "1" }?.get("date")
            ?: throw Exception("学期 $term 第一周星期一日期缺失")
    }

    suspend fun getExamList(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        term: String,
    ): List<ExamInfo> = withSession(cookies, webVpn, "获取考试安排") { session ->
        Exam(session).getExamList(term).map {
            ExamInfo(
                location = it["location"],
                time = it["time"].orEmpty(),
                date = it["date"].orEmpty(),
                seatId = it["seat_id"],
                ksmc = it["ksmc"],
                termCode = it["term_code"],
                courseCode = it["course"],
                teacherName = it["teacher"],
                kch = it["kch"],
            )
        }
    }

    suspend fun getCourseSchedule(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        term: String,
    ): List<CourseForSchedule> = withSession(cookies, webVpn, "获取课程表") { session ->
        // Course.getCourses 内部不初始化应用会话, 先访问应用首页
        session.get(
            "${Config.Urls.active["jxzxehall_app"]}/jwapp/sys/wdkbby/*default/index.do",
            headers = Config.Headers.jxzxehall,
        )
        val rows = Course(session).getCourses(term)["data"]
            ?: throw Exception("教学中心未返回本科课程表")
        return@withSession gson.fromJson(rows.toString(), Array<CourseForSchedule>::class.java).toList()
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
            logger.err("SchoolJxzxehallService", "$action 失败: ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }
}
