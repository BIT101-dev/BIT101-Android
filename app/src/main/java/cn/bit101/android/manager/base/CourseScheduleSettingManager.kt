package cn.bit101.android.manager.base

import cn.bit101.android.manager.basic.SettingItem
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime

data class TimeTableItem(
    val startTime: LocalTime,
    val endTime: LocalTime,
) : Serializable

typealias TimeTable = List<TimeTableItem>

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

fun TimeTable.toTimeTableString(): String {
    return this.joinToString("\n") {
        "${it.startTime}, ${it.endTime}"
    }
}

interface CourseScheduleSettingManager {
    val term: SettingItem<String>
    val firstDay: SettingItem<LocalDate?>
    val showSaturday: SettingItem<Boolean>
    val showSunday: SettingItem<Boolean>
    val showBorder: SettingItem<Boolean>
    val highlightToday: SettingItem<Boolean>
    val showDivider: SettingItem<Boolean>
    val showCurrentTime: SettingItem<Boolean>
    val timeTable: SettingItem<TimeTable>
}