package cn.bit101.android.features.common.utils

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
        return if(LocalDateTime.now().year == time?.year) {
            time.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm:ss"))
        } else time?.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"))
    }

    fun formatDate(time: LocalDateTime?): String? {
        return if(LocalDateTime.now().year == time?.year) {
            time.format(DateTimeFormatter.ofPattern("MM月dd日"))
        } else time?.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
    }

    fun format(time: String): String? {
        return format(formatTime(time))
    }

    fun formatDate(time: String): String? {
        return formatDate(formatTime(time))
    }

    fun calculateTimeDiff(time: LocalDateTime): String {
        val now = LocalDateTime.now()
        // 转换到东八区
        val timeSecond = time.toEpochSecond(ZoneOffset.UTC) + 8 * 60 * 60
        val nowSecond = now.toEpochSecond(ZoneOffset.UTC) + 8 * 60 * 60

        val second = nowSecond - timeSecond
        val minute = second / 60
        val hour = minute / 60
        val day = hour / 24
        val week = day / 7
        val month = day / 30
        val year = month / 12

        return when {
            year > 0 -> "${year}年前"
            month > 0 -> "${month}月前"
            week > 0 -> "${week}周前"
            day > 0 -> "${day}天前"
            hour > 0 -> "${hour}小时前"
            minute > 0 -> "${minute}分钟前"
            second > 0 -> "${second}秒前"
            else -> "刚刚"
        }
    }

}