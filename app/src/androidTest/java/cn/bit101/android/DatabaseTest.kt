package cn.bit101.android

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.CourseScheduleDao
import cn.bit101.android.net.school.getCourseSchedule
import cn.bit101.android.viewmodel.course2db
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author flwfdd
 * @date 2023/3/31 18:14
 * @description _(:з」∠)_
 */

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: BIT101Database
    private lateinit var dao: CourseScheduleDao

    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BIT101Database::class.java).build()
        dao = db.courseScheduleDao()
    }

    @After
    fun close() {
        db.close()
    }

    @Test
    fun test() {
        runBlocking {
            val courses = getCourseSchedule()
            courses!!.courseList.forEach {
                dao.insert(course2db(it))
            }
            println(dao.getWeek("2022-2023-2", 1).first())
        }
    }
}

