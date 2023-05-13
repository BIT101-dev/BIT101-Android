package cn.bit101.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * @author flwfdd
 * @date 2023/3/31 17:49
 * @description _(:з」∠)_
 */

@Database(
    entities = [CourseScheduleEntity::class, DDLScheduleEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BIT101Database : RoomDatabase() {
    abstract fun courseScheduleDao(): CourseScheduleDao
    abstract fun DDLScheduleDao(): DDLScheduleDao
}


// 时间数据格式转换器
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }
}
