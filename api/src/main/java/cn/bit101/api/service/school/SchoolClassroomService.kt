package cn.bit101.api.service.school

import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.ClassroomInfo
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.api.jxzxehall.Classroom
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 基于 BIT-Login SDK 的空教室查询服务
 */
class SchoolClassroomService(
    private val logger: Logger = emptyLogger,
) {
    suspend fun getClassroomOccupancy(
        cookies: List<SchoolCookie>,
        webVpn: Boolean,
        buildingCode: String,
        semester: String? = null,
        week: Int? = null,
        dateStr: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    ): List<ClassroomInfo> {
        val session = openSchoolSession(cookies, webVpn)
        try {
            val rows = Classroom(session).getOccupancy(
                dateStr = dateStr,
                semester = semester,
                week = week,
                buildingCode = buildingCode,
            )

            return rows.map { row ->
                val status = row["status"] as? Map<*, *> ?: emptyMap<Any, Any>()
                val busyTimes = status.mapNotNull { (section, info) ->
                    val state = (info as? Map<*, *>)?.get("state") as? String
                    if (state != null && state != "空闲") section as? Int else null
                }.sorted()

                ClassroomInfo(
                    classroomName = row["name"] as? String ?: "",
                    busyTimeStr = busyTimes.takeIf { it.isNotEmpty() }?.joinToString(","),
                )
            }
        } catch (e: Throwable) {
            logger.err("SchoolClassroomService", "查询空教室失败: ${e.message}")
            throw e
        } finally {
            session.close()
        }
    }
}
