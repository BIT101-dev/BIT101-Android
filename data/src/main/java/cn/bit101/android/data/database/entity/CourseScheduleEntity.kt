package cn.bit101.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.bit101.api.model.common.CourseForSchedule

/**
 * 课程表课程
 */
@Entity(tableName = "course_schedule")
data class CourseScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    /**
     * 学期
     */
    val term: String,

    /**
     * 课程名
     */
    val name: String,

    /**
     * 授课教师 逗号分隔
     */
    val teacher: String,

    /**
     * 教室
     */
    val classroom: String,

    /**
     * 上课时空描述
     */
    val description: String,

    /**
     * 上课周次 形如[1][2][3][4][5][6]
     */
    val weeks: String,

    /**
     * 星期几
     */
    val weekday: Int,

    /**
     * 开始节次
     */
    val start_section: Int,

    /**
     * 结束节次
     */
    val end_section: Int,

    /**
     * 校区
     */
    val campus: String,

    /**
     * 课程号
     */
    val number: String,

    /**
     * 学分
     */
    val credit: Int,

    /**
     * 学时
     */
    val hour: Int,

    /**
     * 课程性质 必修选修什么的
     */
    val type: String,

    /**
     * 课程类别 文化课实践课什么的
     */
    val category: String,

    /**
     * 开课单位
     */
    val department: String, // 开课单位
)

/**
 * 课程表课程转换为数据库实体
 */
internal fun CourseForSchedule.toEntity(): CourseScheduleEntity {
    var weeks = ""
    this.SKZC?.forEachIndexed { index, c ->
        if (c == '1') {
            weeks += "[${index + 1}]"
        }
    }
    return CourseScheduleEntity(
        0,
        this.XNXQDM ?: "",
        this.KCM ?: "",
        this.SKJS ?: "",
        this.JASMC ?: "",
        this.YPSJDD ?: "",
        weeks,
        this.SKXQ ?: 0,
        this.KSJC ?: 0,
        this.JSJC ?: 0,
        this.XXXQMC ?: "",
        this.KCH ?: "",
        this.XF ?: 0,
        this.XS ?: 0,
        this.KCXZDM_DISPLAY ?: "",
        this.KCLBDM_DISPLAY ?: "",
        this.KKDWDM_DISPLAY ?: ""
    )
}