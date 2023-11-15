package cn.bit101.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.bit101.android.database.dao.CoursesDao
import cn.bit101.android.database.dao.DDLScheduleDao
import cn.bit101.android.database.dao.CourseScheduleDao
import cn.bit101.android.database.entity.CourseEntity
import cn.bit101.android.database.entity.DDLScheduleEntity
import cn.bit101.android.database.entity.FirstDayEntity
import cn.bit101.android.database.entity.TermEntity

/**
 * @author flwfdd
 * @date 2023/3/31 17:49
 * @description _(:з」∠)_
 */

@Database(
    entities = [
        CourseEntity::class,
        DDLScheduleEntity::class,
        FirstDayEntity::class,
        TermEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BIT101Database : RoomDatabase() {
    abstract fun coursesDao(): CoursesDao
    abstract fun DDLScheduleDao(): DDLScheduleDao
    abstract fun courseScheduleDao(): CourseScheduleDao
}