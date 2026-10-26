package cn.bit101.android.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationsTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BIT101Database::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun upgradeFromVersion2RebuildsCourseCacheAndPreservesCustomDdl() {
        helper.createDatabase("migration-2.db", 2).use { seedLegacyData(it) }

        helper.runMigrationsAndValidate(
            "migration-2.db", 4, true, MIGRATION_2_4,
        ).use { verifyRebuiltCache(it) }
    }

    @Test
    fun upgradeFromLegacyVersion3RebuildsCourseCache() {
        helper.createDatabase("migration-3.db", 3).use { db ->
            db.execSQL("DROP TABLE course_schedule")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `course_schedule` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `term` TEXT NOT NULL, `name` TEXT NOT NULL, `teacher` TEXT NOT NULL, `classroom` TEXT NOT NULL, `description` TEXT NOT NULL, `weeks` TEXT NOT NULL, `weekday` INTEGER NOT NULL, `start_section` INTEGER NOT NULL, `end_section` INTEGER NOT NULL, `campus` TEXT NOT NULL, `number` TEXT NOT NULL, `credit` INTEGER NOT NULL, `hour` INTEGER NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `department` TEXT NOT NULL)""")
            seedLegacyData(db)
        }

        helper.runMigrationsAndValidate(
            "migration-3.db", 4, true, MIGRATION_3_4,
        ).use { verifyRebuiltCache(it) }
    }

    private fun seedLegacyData(db: SupportSQLiteDatabase) {
        db.execSQL("""INSERT INTO course_schedule (id, term, name, teacher, classroom, description, weeks, weekday, start_section, end_section, campus, number, credit, hour, type, category, department) VALUES (7, '2026-2027-1', '旧课程', '教师', '教室', '备注', '[1][2]', 1, 1, 2, '校区', 'COURSE-1', 2.5, 36, '必修', '类别', '学院')""")
        db.execSQL("INSERT INTO exam_schedule (id, term, name, courseId, teacher, classroom, date, beginTime, endTime, examMode, seatId) VALUES (1, '2026-2027-1', '旧考试', 'COURSE-1', '教师', '教室', 0, 0, 0, '集中', '1')")
        db.execSQL("INSERT INTO ddl_schedule (id, `group`, uid, title, text, time, done) VALUES (1, 'main', 'custom', '自定义日程', '保留', 0, 0)")
        db.execSQL("INSERT INTO ddl_schedule (id, `group`, uid, title, text, time, done) VALUES (2, 'lexue', 'imported', '旧导入日程', '重新获取', 0, 0)")
    }

    private fun verifyRebuiltCache(db: SupportSQLiteDatabase) {
        db.query("SELECT COUNT(*) FROM course_schedule").use {
            assertTrue(it.moveToFirst())
            assertEquals(0L, it.getLong(0))
        }
        db.query("SELECT COUNT(*) FROM exam_schedule").use {
            assertTrue(it.moveToFirst())
            assertEquals(0L, it.getLong(0))
        }
        assertEquals(1L, ddlCount(db, "custom"))
        assertEquals(0L, ddlCount(db, "imported"))
    }

    private fun ddlCount(db: SupportSQLiteDatabase, uid: String): Long =
        db.query("SELECT COUNT(*) FROM ddl_schedule WHERE uid = ?", arrayOf(uid)).use {
            assertTrue(it.moveToFirst())
            it.getLong(0)
        }
}
