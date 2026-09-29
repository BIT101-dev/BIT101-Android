package cn.bit101.android.data.eclass

import cn.bit101.api.model.http.eclass.GetEclassActivitiesDataModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** 课程中心活动到 DDL 条目的转换逻辑。 */
object EclassDdlLogic {

    /** 课程中心在 DDL 表中的来源标识。 */
    const val GROUP = "eclass"

    /** 课程中心地址。 */
    const val BASE_URL = "https://zy-eclass.bit.edu.cn"

    /** App 内 WebView 登录入口。 */
    const val LOGIN_URL = "$BASE_URL/user/index"

    /** 资料活动类型。 */
    private const val TYPE_MATERIAL = "material"

    private val TIME_FORMATS = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
    )

    /** 活动是否为有截止时间的作业。 */
    fun isHomework(activity: GetEclassActivitiesDataModel.Activity): Boolean {
        if (activity.type.equals(TYPE_MATERIAL, ignoreCase = true)) return false
        if (deadlineOf(activity) == null) return false
        return activity.submitTimes != null ||
            activity.isReviewHomework != null ||
            activity.lateSubmissionCount != null
    }

    /** 解析接口时间；无时区值按本地时间，有时区值换算到北京时间。 */
    fun parseTime(raw: String?): LocalDateTime? {
        val text = raw?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
            ?: return null

        runCatching {
            return java.time.OffsetDateTime.parse(text)
                .atZoneSameInstant(java.time.ZoneId.of("Asia/Shanghai"))
                .toLocalDateTime()
        }
        runCatching { return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        runCatching {
            return java.time.Instant.parse(text)
                .atZone(java.time.ZoneId.of("Asia/Shanghai"))
                .toLocalDateTime()
        }

        TIME_FORMATS.forEach { fmt ->
            runCatching { return LocalDateTime.parse(text, fmt) }
        }
        return null
    }

    /** 优先使用截止时间，缺失时回退到可见截止时间。 */
    fun deadlineOf(activity: GetEclassActivitiesDataModel.Activity): LocalDateTime? =
        parseTime(activity.endTime) ?: parseTime(activity.visibleEndAt)

    /** 将作业映射为 DDL 条目；非作业返回 null。 */
    fun toDdlItem(
        activity: GetEclassActivitiesDataModel.Activity,
        courseName: String,
        now: LocalDateTime,
        includeOverdue: Boolean = true,
    ): EclassDdlItem? {
        if (!isHomework(activity)) return null
        val deadline = deadlineOf(activity) ?: return null
        if (!includeOverdue && deadline.isBefore(now)) return null

        return EclassDdlItem(
            uid = uid(activity.id),
            title = activity.title.trim().ifEmpty { "未命名作业" },
            text = courseName,
            time = deadline,
        )
    }

    /** 生成 DDL 去重键。 */
    fun uid(activityId: Int): String = "$GROUP:$activityId"

    /** 转换活动并按截止时间排序。 */
    fun toDdlItems(
        activities: List<GetEclassActivitiesDataModel.Activity>,
        courseName: String,
        now: LocalDateTime,
        includeOverdue: Boolean = true,
    ): List<EclassDdlItem> =
        activities
            .mapNotNull { toDdlItem(it, courseName, now, includeOverdue) }
            .sortedBy { it.time }
}

/** 待写入 DDL 表的课程中心作业。 */
data class EclassDdlItem(
    /** 去重键。 */
    val uid: String,
    val title: String,
    val text: String,
    val time: LocalDateTime,
)
