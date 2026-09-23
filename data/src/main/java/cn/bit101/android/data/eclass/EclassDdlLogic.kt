package cn.bit101.android.data.eclass

import cn.bit101.api.model.http.eclass.GetEclassActivitiesDataModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 课程中心（eclass）→ DDL 的**纯逻辑**。
 *
 * 全部是纯函数，便于单测（沿用项目里「纯逻辑一律抽 `*Logic`」的约定）。
 * 取数（HTTP、数据库）在 `EclassRepo` 里，本类不碰。
 *
 * ## 判定作业的方式（重要）
 *
 * 课程中心的「课程动态」把**资料和作业放在同一张表**里，靠 `type` 与若干字段区分。
 *
 * ⚠️ 这里刻意**不枚举 `type` 的取值**。原因：2026-09-23 实测时开学第 4 周，
 * 课程里只有资料、没有任何作业，所以**拿不到作业的 type 真实值**
 * （契约文档 `docs/ddl-source-contract.md` 记了当时试过哪些办法）。
 * 猜一个枚举值写进来，一旦猜错就是「一个作业都收不到」且很难排查。
 *
 * 改为判断**作业特有字段是否存在**：
 * `submitTimes` / `isReviewHomework` / `lateSubmissionCount` 只在需要提交的作业上出现。
 * 同时要求**有截止时间** —— 没有截止时间的活动不该进 DDL 列表。
 *
 * 唯一已知的 `type` 取值是资料的 `"material"`，直接排除掉。
 */
object EclassDdlLogic {

    /** eclass 来源在 `ddl_schedule.group` 里的标识。 */
    const val GROUP = "eclass"

    /**
     * 站点根地址（cookie 同步的域）。
     */
    const val BASE_URL = "https://zy-eclass.bit.edu.cn"

    /**
     * 登录入口。
     *
     * ⚠️ 必须用 **App 内 WebView** 打开，不能用系统浏览器 ——
     * 只有 App 内的 WebView 才与 `LoginStatus.cookieManager` 共享 cookie
     * （见 `WebViewCookieSync`）；用系统浏览器登录的话，我们这边拿不到会话。
     */
    const val LOGIN_URL = "$BASE_URL/user/index"

    /** 已知的资料类型（实测值），直接排除。 */
    private const val TYPE_MATERIAL = "material"

    private val TIME_FORMATS = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
    )

    /**
     * 这条课程动态是不是「要交的作业」。
     *
     * 规则（保守，宁可漏也不要误报 —— DDL 列表里混进资料比少一条作业更烦人）：
     * 1. 已知的资料类型 → 否
     * 2. 没有可解析的截止时间 → 否
     * 3. 出现了作业特有字段 → 是
     */
    fun isHomework(activity: GetEclassActivitiesDataModel.Activity): Boolean {
        if (activity.type.equals(TYPE_MATERIAL, ignoreCase = true)) return false
        // ⚠️ 这里必须用 deadlineOf 而不是只看 endTime —— 它会在 endTime 缺失时
        // 回落到 visibleEndAt。曾经只判 endTime，导致「只有 visibleEndAt 的作业」
        // 先被判成非作业、又被 toDdlItem 丢弃，两处口径不一致
        //（单测 `没有 endTime 时回落到 visibleEndAt` 逮到过）。
        if (deadlineOf(activity) == null) return false
        return activity.submitTimes != null ||
            activity.isReviewHomework != null ||
            activity.lateSubmissionCount != null
    }

    /**
     * 解析接口返回的时间字符串。
     *
     * 接口的时间格式在不同接口/不同字段上并不统一（实测见过 `yyyy-MM-dd HH:mm:ss`
     * 与 ISO 两种），所以按多个格式依次尝试；也顺手处理 `"null"` 这种字符串化的空值。
     *
     * 返回的是**本地时间**：接口给的就是北京时间，不做时区换算
     * （⚠️ 与项目里 `DateTimeUtils` 的 UTC+8 硬偏移口径保持一致，别在这里引入第二种算法）。
     */
    fun parseTime(raw: String?): LocalDateTime? {
        val text = raw?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
            ?: return null

        // ISO 形式（2026-10-01T23:59:00 / 带 Z / 带 +08:00）先单独处理
        runCatching { return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        runCatching {
            return java.time.OffsetDateTime.parse(text).toLocalDateTime()
        }
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

    /** 活动截止时间（可解析时）。 */
    fun deadlineOf(activity: GetEclassActivitiesDataModel.Activity): LocalDateTime? =
        parseTime(activity.endTime) ?: parseTime(activity.visibleEndAt)

    /**
     * 把一条作业转成待写入 `ddl_schedule` 的条目；不是作业或已过期返回 null。
     *
     * @param courseName 所属课程名，写进 `text` 供列表展示
     * @param now 当前时间（传入而非内部取，便于单测）
     * @param includeOverdue 是否保留已过期的作业（同步时用它决定是否清理旧数据）
     */
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
            // uid 用「来源前缀 + 活动 id」：活动 id 在同一站点内唯一，
            // 加前缀是为了将来多源并存时不会跟乐学的 uid 撞
            uid = uid(activity.id),
            title = activity.title.trim().ifEmpty { "未命名作业" },
            text = courseName,
            time = deadline,
        )
    }

    /** 稳定的去重键。 */
    fun uid(activityId: Int): String = "$GROUP:$activityId"

    /**
     * 批量转换 + 按截止时间升序。
     *
     * 同一门课里可能有多条作业，排序放在这里而不是 UI ——
     * 组件与 App 都要读，口径必须一致。
     */
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

/**
 * 待写入 `ddl_schedule` 的一条 eclass 作业。
 *
 * 刻意不复用 `DDLScheduleEntity`（那是带 Room 注解的实体），
 * 这样纯逻辑层不依赖数据库类型、单测不需要 Room。
 */
data class EclassDdlItem(
    /** 去重键，形如 `eclass:12345`。 */
    val uid: String,
    /** 作业标题。 */
    val title: String,
    /** 所属课程名（展示用）。 */
    val text: String,
    /** 截止时间。 */
    val time: LocalDateTime,
)
