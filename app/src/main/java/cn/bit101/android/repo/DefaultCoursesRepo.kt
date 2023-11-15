package cn.bit101.android.repo

import android.util.Log
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.FirstDayEntity
import cn.bit101.android.database.entity.toEntity
import cn.bit101.android.database.entity.toTermEntity
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.CoursesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DefaultCoursesRepo @Inject constructor(
    private val database: BIT101Database,
) : CoursesRepo {
    private suspend fun getCoursesFromNet(
        term: String,
        save: Boolean,
    ) = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getAppConfig()
        BIT101API.schoolJxzxehallapp.switchLang()

        var newTerm = term

        // 获取学期
        if (newTerm.isBlank()) {
            newTerm = BIT101API.schoolJxzxehallapp.getCurrentTerm().body()?.datas?.dqxnxq?.rows?.get(0)?.DM
                ?: throw Exception("get term error")
        }

        val courseList = BIT101API.schoolJxzxehallapp.getSchedule(newTerm).body()?.datas?.cxxszhxqkb?.rows
            ?: throw Exception("get course list error")

        val res = courseList.map { it.toEntity() }

        if(save) {
            // 删掉这个学期的所有课程
            database.coursesDao().deleteCoursesByTerm(term)

            // 放入进现在获得的所有课程
            database.coursesDao().insertCourses(res)
        }
        res
    }

    private suspend fun getCoursesFromLocal() = withContext(Dispatchers.IO) {
        database.coursesDao().getAllCourses().first()
    }

    private suspend fun getCoursesFromLocal(
        term: String,
    ) = withContext(Dispatchers.IO) {
        database.coursesDao().getCoursesByTerm(term).first()
    }

    private suspend fun getCoursesFromLocal(
        term: String,
        week: Int
    ) = withContext(Dispatchers.IO) {
        database.coursesDao().getCoursesByTermWeek(term, week).first()
    }

    override suspend fun getCourses(
        forceNet: Boolean,
        autoSave: Boolean
    ) = withContext(Dispatchers.IO) {
        getCoursesFromLocal()
    }

    override suspend fun getCourses(
        term: String,
        forceNet: Boolean,
        autoSave: Boolean,
    ) = withContext(Dispatchers.IO) {
        try {
            // 从本地获取
            if(forceNet) throw Exception("force network")
            val courses = getCoursesFromLocal(term)
            if(courses.isEmpty()) throw Exception("local courses is empty")
            courses
        } catch (e: Exception) {
            getCoursesFromNet(term, autoSave)
        }
    }

    override suspend fun getCourses(
        term: String,
        week: Int,
        forceNet: Boolean,
        autoSave: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            if(forceNet) throw Exception("force network")
            val courses = database.coursesDao().getCoursesByTermWeek(term, week).first()
            if(courses.isEmpty()) throw Exception("local courses is empty")
            courses
        } catch (e: Exception) {
            val res = getCoursesFromNet(term, autoSave)
            res.takeWhile { it.weeks.contains("[$week]") }
        }
    }

    private suspend fun getTermListFromNet(
        save: Boolean
    ) = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getAppConfig()
        val terms = BIT101API.schoolJxzxehallapp.getTerms().body()?.datas?.xnxqcx?.rows?.map { it.DM }
            ?: throw Exception("Get Term List Error")
        Log.i("SchoolSchedule", "Get Term List Success: $terms")
        if(save) {
            database.courseScheduleDao().deleteAll()
            database.courseScheduleDao().insertTerms(terms.map { it.toTermEntity() })
        }
        terms
    }

    private suspend fun getTermListFromLocal() = withContext(Dispatchers.IO) {
        database.courseScheduleDao().getTerms()
    }

    override suspend fun getTermList(
        forceNet: Boolean,
        autoSave: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            if(forceNet) throw Exception("force network")
            getTermListFromLocal()
        } catch (e: Exception) {
            getTermListFromNet(autoSave)
        }
    }

    private suspend fun getFirstDayFromNet(
        term: String,
        save: Boolean,
    ) = withContext(Dispatchers.IO) {
        val data = BIT101API.schoolJxzxehallapp.getWeekAndDate(
            requestParamStr = "{\"XNXQDM\":\"$term\",\"ZC\":\"1\"}"
        ).body()?.data ?: throw Exception("get first day response error")

        var firstDay: LocalDate? = null

        Log.i("SchoolSchedule", "Get First Day Success: $data")

        data.forEach {
            if(it.week == 1) {
                firstDay = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                return@forEach
            }
        }

        if(firstDay == null) throw Exception("get first day error")

        if(save) {
            database.courseScheduleDao().upsertFirstDay(FirstDayEntity(term, firstDay!!))
        }

        firstDay!!
    }

    private suspend fun getFirstDayFromLocal(
        term: String
    ) = withContext(Dispatchers.IO) {
        database.courseScheduleDao().getFirstDayOf(term)
    }

    override suspend fun getFirstDay(
        term: String,
        forceNet: Boolean,
        autoSave: Boolean,
    ) = withContext(Dispatchers.IO) {
        try {
            if(forceNet) throw Exception("force network")
            getFirstDayFromLocal(term)
        } catch (e: Exception) {
            getFirstDayFromNet(term, autoSave)
        }
    }

    override suspend fun getWeekAndDay(
        term: String
    ) = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getWeekAndDate(
            requestParamStr = "{\"XNXQDM\":\"$term\",\"ZC\":\"1\"}"
        ).body()?.data ?: throw Exception("get week and day error")

    }


}