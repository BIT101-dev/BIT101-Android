package cn.bit101.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.bit101.android.data.database.dao.CoursesDao
import cn.bit101.android.data.database.dao.DDLScheduleDao
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.DDLScheduleEntity

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
internal abstract class BIT101Database : RoomDatabase() {
    abstract fun coursesDao(): CoursesDao
    abstract fun DDLScheduleDao(): DDLScheduleDao
}