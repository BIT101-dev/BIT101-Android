package cn.bit101.android.config.setting.base

import cn.bit101.android.config.common.SettingItem
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime

/**
 * 时间表项
 */
data class TimeTableItem(
    val startTime: LocalTime,
    val endTime: LocalTime,
) : Serializable

/**
 * 时间表
 */
typealias TimeTable = List<TimeTableItem>

/**
 * 将字符串转换为时间表，格式错误时抛出异常
 */
fun String.toTimeTable(): TimeTable {
    val timeTable = mutableListOf<TimeTableItem>()
    this.split("\n").forEach {
        if (it.isBlank()) return@forEach
        val x = it.split(",")
        timeTable.add(
            TimeTableItem(
                LocalTime.parse(x[0].trim()),
                LocalTime.parse(x[1].trim())
            )
        )
    }

    if(timeTable.isEmpty()) throw IllegalArgumentException("Invalid time table")

    timeTable.forEachIndexed { index, time ->
        if (time.endTime <= time.startTime) throw IllegalArgumentException("Invalid time table")
        if (index != 0) {
            if (time.startTime <= timeTable[index - 1].endTime) throw IllegalArgumentException("Invalid time table")
        }
    }

    return timeTable
}

/**
 * 将时间表转换为字符串
 */
fun TimeTable.toTimeTableString(): String {
    return this.joinToString("\n") {
        "${it.startTime}, ${it.endTime}"
    }
}

/**
 * 课程表设置
 */
interface CourseScheduleSettings {

    /**
     * 学期
     */
    val term: SettingItem<String>

    /**
     * 开学第一天
     */
    val firstDay: SettingItem<LocalDate?>

    /**
     * 是否显示周六
     */
    val showSaturday: SettingItem<Boolean>

    /**
     * 是否显示周日
     */
    val showSunday: SettingItem<Boolean>

    /**
     * 是否显示边框
     */
    val showBorder: SettingItem<Boolean>

    /**
     * 是否高亮今天
     */
    val highlightToday: SettingItem<Boolean>

    /**
     * 是否显示分割线
     */
    val showDivider: SettingItem<Boolean>

    /**
     * 是否显示当前时间线
     */
    val showCurrentTime: SettingItem<Boolean>

    /**
     * 时间表
     */
    val timeTable: SettingItem<TimeTable>
}