package cn.bit101.android.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * @author flwfdd
 * @date 2023/3/31 17:49
 * @description _(:з」∠)_
 */

@Database(entities = [CourseScheduleEntity::class], version = 1, exportSchema = false)
abstract class BIT101Database: RoomDatabase() {
    abstract fun courseScheduleDao(): CourseScheduleDao
}