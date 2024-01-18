package cn.bit101.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.bit101.android.database.dao.CoursesDao
import cn.bit101.android.database.dao.DDLScheduleDao
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.database.entity.DDLScheduleEntity

/**
 * @author flwfdd
 * @date 2023/3/31 17:49
 * @description _(:з」∠)_
 */

@Database(
    entities = [
        CourseScheduleEntity::class,
        DDLScheduleEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BIT101Database : RoomDatabase() {
    abstract fun coursesDao(): CoursesDao
    abstract fun DDLScheduleDao(): DDLScheduleDao
}