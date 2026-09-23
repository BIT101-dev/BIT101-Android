package cn.bit101.api.model.http.eclass

/**
 * 课程中心（eclass / 延河课堂，`zy-eclass.bit.edu.cn`）的响应模型。
 *
 * ⚠️ 这里字段用 **camelCase** —— `Bit101ApiFactory` 的 Gson 配了
 * `FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES`，会自动与接口的 snake_case
 * （`recently_visited_courses`、`end_time`、`submit_times`…）对应。
 *
 * 字段依据 2026-09-23 的真实响应整理，见 `docs/ddl-source-contract.md`。
 */

/** `GET /api/user/recently-visited-courses` —— 最近访问的课程。 */
class GetEclassCoursesDataModel private constructor() {

    data class Response(
        val visitedCourses: List<Course> = emptyList(),
    )

    /**
     * 课程。
     *
     * ⚠️ 除 [id] 与 [name] 外的字段都给了默认值：接口在不同课程上会缺字段，
     * 而 DDL 只需要「课程 id + 课名」，缺了别的也不该整门课丢掉。
     */
    data class Course(
        /** 课程 id，用于 `/api/courses/{id}/activities`。 */
        val id: Int,
        /** 课程名，如「操作系统」。 */
        val name: String,
        /** 教务课号。 */
        val courseCode: String? = null,
        val courseType: Int = 0,
        /** 开课学院。 */
        val department: Department? = null,
        /** 课程主页地址。 */
        val url: String? = null,
        val currentUserIsMember: Boolean = false,
    )

    data class Department(
        val name: String? = null,
    )
}

/** `GET /api/courses/{id}/activities` —— 课程动态。 */
class GetEclassActivitiesDataModel private constructor() {

    data class Response(
        val activities: List<Activity> = emptyList(),
    )

    /**
     * 课程活动。**资料与作业在同一张表里**，靠 [type] 与「作业特有字段」区分。
     *
     * 判定作业的逻辑见 `EclassDdlLogic` —— 刻意**不枚举** [type] 的取值，
     * 因为开学前几周课程里只有资料，拿不到作业的 type 真实值；
     * 改判「[submitTimes] / [isReviewHomework] 之类只出现在作业上的字段是否存在」。
     */
    data class Activity(
        val id: Int,
        /** 活动标题（作业名 / 资料名）。 */
        val title: String,
        val type: String? = null,
        val courseId: Int = 0,
        /** 开始时间（字符串，格式见文档；解析时兼容多种写法）。 */
        val startTime: String? = null,
        /** **截止时间** —— DDL 的直接来源。 */
        val endTime: String? = null,
        /** 对学生可见的截止时刻（通常与 [endTime] 相同或更晚）。 */
        val visibleEndAt: String? = null,
        val isClosed: Boolean = false,
        // ── 以下字段「只出现在作业上」，是判定作业的依据 ──
        /** 允许提交的次数。资料不会有这个字段。 */
        val submitTimes: Int? = null,
        val lateSubmissionCount: Int? = null,
        /** 是否是「需批阅的作业」。 */
        val isReviewHomework: Boolean? = null,
    )
}
