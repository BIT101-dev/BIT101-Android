package cn.bit101.android.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private fun SupportSQLiteDatabase.recreateCourseSchedule() {
    execSQL("DROP TABLE course_schedule")
    execSQL("""CREATE TABLE IF NOT EXISTS `course_schedule` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `term` TEXT NOT NULL, `name` TEXT NOT NULL, `teacher` TEXT NOT NULL, `classroom` TEXT NOT NULL, `description` TEXT NOT NULL, `weeks` TEXT NOT NULL, `weekday` INTEGER NOT NULL, `start_section` INTEGER NOT NULL, `end_section` INTEGER NOT NULL, `campus` TEXT NOT NULL, `number` TEXT NOT NULL, `credit` REAL NOT NULL, `hour` INTEGER NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `department` TEXT NOT NULL)""")
}

private fun SupportSQLiteDatabase.upgradeLegacySchedule() {
    recreateCourseSchedule()
    execSQL("DELETE FROM exam_schedule")
    execSQL("DELETE FROM ddl_schedule WHERE `group` = 'lexue'")
    execSQL("ALTER TABLE ddl_schedule ADD COLUMN ownerSid TEXT NOT NULL DEFAULT ''")
    execSQL("DROP INDEX IF EXISTS `index_ddl_schedule_uid`")
    execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ddl_schedule_ownerSid_uid` ON `ddl_schedule` (`ownerSid`, `uid`)")
    execSQL("CREATE INDEX IF NOT EXISTS `index_ddl_schedule_group` ON `ddl_schedule` (`group`)")
}

internal val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(db: SupportSQLiteDatabase) = db.upgradeLegacySchedule()
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) = db.upgradeLegacySchedule()
}
