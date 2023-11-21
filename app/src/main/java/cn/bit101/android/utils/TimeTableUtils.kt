package cn.bit101.android.utils

import java.time.LocalTime

data class TimeTableItem(
    val startTime: LocalTime,
    val endTime: LocalTime,
)

object TimeTableUtils {
    fun checkTimeTable(timeTable: String): Boolean {
        return try {
            val l = parseTimeTable(timeTable)
            if (l.isEmpty()) return false
            l.forEachIndexed { index, time ->
                if (time.endTime <= time.startTime) return false
                if (index != 0) {
                    if (time.startTime <= l[index - 1].endTime) return false
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun parseTimeTable(s: String): List<TimeTableItem> {
        val timeTable = mutableListOf<TimeTableItem>()

        s.split("\n").forEach {
            if (it.isBlank()) return@forEach
            val x = it.split(",")
            timeTable.add(
                TimeTableItem(
                    LocalTime.parse(x[0]),
                    LocalTime.parse(x[1])
                )
            )
        }
        return timeTable
    }
}