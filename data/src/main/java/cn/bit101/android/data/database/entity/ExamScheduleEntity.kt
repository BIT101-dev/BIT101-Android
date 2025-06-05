package cn.bit101.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import cn.bit101.api.model.common.ExamInfo
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 考试安排
 */
@Entity(tableName = "exam_schedule")
data class ExamScheduleEntity (
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
     * 课程号
     */
    val courseId: String,

    /**
     * 授课教师
     */
    val teacher: String,

    /**
     * 教室
     */
    val classroom: String,

    /**
     * 考试日期
     */
    val date: LocalDate,

    /**
     * 考试开始时间
     */
    val beginTime: LocalTime,

    /**
     * 考试结束时间
     */
    val endTime: LocalTime,

    /**
     * 分散考试 / 集中考试等
     */
    val examMode: String,

    /**
     * 座位号
     */
    val seatId: String,
)

/**
 * 考试安排转换为数据库实体
 */
internal fun ExamInfo.toEntity(): ExamScheduleEntity {
    return ExamScheduleEntity(
        0,
        this.termCode.orEmpty(),
        this.courseCode
            ?.takeWhile { it != ']' }
            ?.takeLastWhile { it != '[' }
            .orEmpty(),
        this.kch.orEmpty(),
        this.teacherName.orEmpty(),
        this.location.orEmpty(),
        LocalDate.parse(this.date.takeWhile { it != ' ' }, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        LocalTime.parse(this.time
            .dropLastWhile { it != '-' }
            .dropLast(1)
            .dropWhile { it != ' ' }
            .drop(1),
            DateTimeFormatter.ofPattern("HH:mm")),
        LocalTime.parse(this.time
            .dropLastWhile { it != '(' }
            .dropLast(1)
            .takeLastWhile { it != '-' },
            DateTimeFormatter.ofPattern("HH:mm")
        ),
        this.ksmc.orEmpty(),
        this.seatId.orEmpty(),
    )
}