package cn.bit101.android.data.database

import androidx.room.migration.Migration
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.db.SupportSQLiteDatabase

/** 学分支持小数；完整保留旧课程和其他日程表。 */
internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS `course_schedule_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `term` TEXT NOT NULL, `name` TEXT NOT NULL, `teacher` TEXT NOT NULL, `classroom` TEXT NOT NULL, `description` TEXT NOT NULL, `weeks` TEXT NOT NULL, `weekday` INTEGER NOT NULL, `start_section` INTEGER NOT NULL, `end_section` INTEGER NOT NULL, `campus` TEXT NOT NULL, `number` TEXT NOT NULL, `credit` REAL NOT NULL, `hour` INTEGER NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `department` TEXT NOT NULL)""")
        db.execSQL("""INSERT INTO `course_schedule_new` (`id`, `term`, `name`, `teacher`, `classroom`, `description`, `weeks`, `weekday`, `start_section`, `end_section`, `campus`, `number`, `credit`, `hour`, `type`, `category`, `department`) SELECT `id`, `term`, `name`, `teacher`, `classroom`, `description`, `weeks`, `weekday`, `start_section`, `end_section`, `campus`, `number`, `credit`, `hour`, `type`, `category`, `department` FROM `course_schedule`""")
        db.execSQL("DROP TABLE `course_schedule`")
        db.execSQL("ALTER TABLE `course_schedule_new` RENAME TO `course_schedule`")
    }
}

/** 旧版导入日程没有账号归属，重新订阅恢复；保留自定义日程。 */
internal class DdlOwnershipMigration : AutoMigrationSpec {
    override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL("DELETE FROM ddl_schedule WHERE `group` = 'lexue' AND ownerSid = ''")
    }
}
