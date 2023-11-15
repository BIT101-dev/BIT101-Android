package cn.bit101.android.utils

import cn.bit101.api.model.common.TIME_FORMATTER
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    fun formatTime(time: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(time, TIME_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    fun format(time: LocalDateTime?): String? {
        return time?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    fun calculateTimeDiff(time: LocalDateTime): String {
        val now = LocalDateTime.now()
        // 转换到东八区
        val timeSecond = time.toEpochSecond(ZoneOffset.UTC) + 8 * 60 * 60
        val nowSecond = now.toEpochSecond(ZoneOffset.UTC) + 8 * 60 * 60
        val diff = nowSecond - timeSecond

        val minute = diff / 60
        val hour = minute / 60
        val day = hour / 24
        val week = day / 7

        return when {
            minute < 60 -> "刚刚"
            hour < 1 -> "${minute}分钟前"
            day < 1 -> "${hour}小时前"
            week < 7 -> "${day}天前"
            else -> time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }
    }

}