package cn.bit101.android.data.eclass

import cn.bit101.api.model.http.eclass.GetEclassActivitiesDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * `EclassDdlLogic` 的单测。
 *
 * 重点锁两件事：
 * 1. **作业判定** —— 它直接决定 DDL 列表里有什么，判错就是「要么一条没有、
 *    要么全是资料」，而这在真机上很难一眼看出是判定错还是接口没数据；
 * 2. **时间解析** —— 接口的时间格式在不同字段上不统一，解析失败会让作业静默消失。
 */
class EclassDdlLogicTest {

    private val now: LocalDateTime = LocalDateTime.of(2026, 9, 23, 10, 0)

    private fun activity(
        id: Int = 1,
        title: String = "第三次作业",
        type: String? = "homework",
        endTime: String? = "2026-09-30 23:59:59",
        visibleEndAt: String? = null,
        submitTimes: Int? = 1,
        lateSubmissionCount: Int? = 0,
        isReviewHomework: Boolean? = true,
    ) = GetEclassActivitiesDataModel.Activity(
        id = id,
        title = title,
        type = type,
        endTime = endTime,
        visibleEndAt = visibleEndAt,
        submitTimes = submitTimes,
        lateSubmissionCount = lateSubmissionCount,
        isReviewHomework = isReviewHomework,
    )

    // ── 作业判定 ─────────────────────────────────────────────

    @Test
    fun `带截止时间与提交次数的活动判定为作业`() {
        assertTrue(EclassDdlLogic.isHomework(activity()))
    }

    /** 资料是唯一已知 type 取值的类型，必须排除。 */
    @Test
    fun `资料不算作业`() {
        val material = activity(type = "material")

        assertFalse(EclassDdlLogic.isHomework(material))
    }

    /** 资料哪怕带了这些字段（接口对资料的返回值未经确认）也不该进 DDL。 */
    @Test
    fun `资料即使带提交字段也不算作业`() {
        val material = activity(type = "material", submitTimes = 0, isReviewHomework = false)

        assertFalse(EclassDdlLogic.isHomework(material))
    }

    /** 没有截止时间的活动不需要提醒。 */
    @Test
    fun `没有截止时间的不算作业`() {
        assertFalse(EclassDdlLogic.isHomework(activity(endTime = null)))
    }

    @Test
    fun `截止时间不可解析时不算作业`() {
        assertFalse(EclassDdlLogic.isHomework(activity(endTime = "不是时间")))
    }

    /** 三个作业特有字段任一存在即可（不枚举 type 的核心）。 */
    @Test
    fun `只有 submitTimes 也能判定为作业`() {
        val a = activity(submitTimes = 3, isReviewHomework = null, lateSubmissionCount = null)

        assertTrue(EclassDdlLogic.isHomework(a))
    }

    @Test
    fun `只有 isReviewHomework 也能判定为作业`() {
        val a = activity(submitTimes = null, isReviewHomework = true, lateSubmissionCount = null)

        assertTrue(EclassDdlLogic.isHomework(a))
    }

    /** 一个作业特有字段都没有 → 是资料一类，不进 DDL。 */
    @Test
    fun `无任何作业字段的活动不算作业`() {
        val a = activity(submitTimes = null, isReviewHomework = null, lateSubmissionCount = null)

        assertFalse(EclassDdlLogic.isHomework(a))
    }

    // ── 时间解析 ─────────────────────────────────────────────

    @Test
    fun `解析标准格式`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 30, 23, 59, 59),
            EclassDdlLogic.parseTime("2026-09-30 23:59:59"),
        )
    }

    @Test
    fun `解析到分钟`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 30, 23, 59),
            EclassDdlLogic.parseTime("2026-09-30 23:59"),
        )
    }

    @Test
    fun `解析 ISO 格式`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 30, 23, 59, 0),
            EclassDdlLogic.parseTime("2026-09-30T23:59:00"),
        )
    }

    @Test
    fun `解析斜杠格式`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 30, 23, 59),
            EclassDdlLogic.parseTime("2026/09/30 23:59"),
        )
    }

    @Test
    fun `空值与字符串 null 都返回 null`() {
        assertNull(EclassDdlLogic.parseTime(null))
        assertNull(EclassDdlLogic.parseTime(""))
        assertNull(EclassDdlLogic.parseTime("   "))
        assertNull(EclassDdlLogic.parseTime("null"))
    }

    // ── 映射 ─────────────────────────────────────────────────

    @Test
    fun `映射出稳定的去重键`() {
        val item = EclassDdlLogic.toDdlItem(activity(id = 987), "操作系统", now)!!

        assertEquals("eclass:987", item.uid)
        assertEquals("操作系统", item.text)
        assertEquals(LocalDateTime.of(2026, 9, 30, 23, 59, 59), item.time)
    }

    @Test
    fun `没有 endTime 时回落到 visibleEndAt`() {
        val a = activity(endTime = null, visibleEndAt = "2026-10-01 12:00:00")

        val item = EclassDdlLogic.toDdlItem(a, "操作系统", now)!!

        assertEquals(LocalDateTime.of(2026, 10, 1, 12, 0, 0), item.time)
    }

    @Test
    fun `标题为空时给兜底文案`() {
        val a = activity(title = "   ")

        val item = EclassDdlLogic.toDdlItem(a, "操作系统", now)!!

        assertEquals("未命名作业", item.title)
    }

    @Test
    fun `资料不会映射成条目`() {
        assertNull(EclassDdlLogic.toDdlItem(activity(type = "material"), "操作系统", now))
    }

    @Test
    fun `批量映射按截止时间升序`() {
        val list = listOf(
            activity(id = 1, endTime = "2026-10-05 12:00:00"),
            activity(id = 2, endTime = "2026-09-25 12:00:00"),
            activity(id = 3, endTime = "2026-09-30 12:00:00"),
        )

        val items = EclassDdlLogic.toDdlItems(list, "操作系统", now)

        assertEquals(listOf("eclass:2", "eclass:3", "eclass:1"), items.map { it.uid })
    }

    @Test
    fun `批量映射会剔掉资料与无截止时间的项`() {
        val list = listOf(
            activity(id = 1),
            activity(id = 2, type = "material"),
            activity(id = 3, endTime = null),
        )

        val items = EclassDdlLogic.toDdlItems(list, "操作系统", now)

        assertEquals(listOf("eclass:1"), items.map { it.uid })
    }

    @Test
    fun `includeOverdue 为 false 时丢掉已过期的`() {
        val list = listOf(
            activity(id = 1, endTime = "2026-09-20 12:00:00"), // 已过期
            activity(id = 2, endTime = "2026-09-25 12:00:00"), // 未过期
        )

        val items = EclassDdlLogic.toDdlItems(list, "操作系统", now, includeOverdue = false)

        assertEquals(listOf("eclass:2"), items.map { it.uid })
    }
}
