package cn.bit101.android.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.bit101.android.data.database.dao.CoursesDao
import cn.bit101.android.data.database.dao.DDLScheduleDao
import cn.bit101.android.data.database.dao.ExamsDao
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity

/**
 * @author flwfdd
 * @date 2023/3/31 17:49
 * @description _(:з」∠)_
 */

@Database(
    entities = [
        CourseScheduleEntity::class,
        ExamScheduleEntity::class,
        DDLScheduleEntity::class,
    ],
    version = 2,
//    exportSchema = false  // 不知道为什么原来要禁用, developer.android.com 似乎推荐保留数据库架构历史, 而且启用的话数据库迁移会方便很多, 所以我注释掉了
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
internal abstract class BIT101Database : RoomDatabase() {
    abstract fun coursesDao(): CoursesDao
    abstract fun examsDao(): ExamsDao
    abstract fun DDLScheduleDao(): DDLScheduleDao
}