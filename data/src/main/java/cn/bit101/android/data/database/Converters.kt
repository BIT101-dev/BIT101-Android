package cn.bit101.android.data.database

import androidx.room.TypeConverter
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * 时间、日期数据格式转换器
 */
internal class Converters {
    @TypeConverter
    fun toLocalDataTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun fromLocalDataTime(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? {
        return value?.toEpochDay()
    }

    @TypeConverter
    fun toLocalData(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(value) }
    }

    @TypeConverter
    fun toLocalTime(value: Long?): LocalTime? {
        return value?.let { LocalTime.ofNanoOfDay(value) }
    }
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): Long? {
        return value?.toNanoOfDay()
    }
}
