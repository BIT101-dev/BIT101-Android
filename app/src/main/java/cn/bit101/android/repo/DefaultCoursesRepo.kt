package cn.bit101.android.repo

import android.util.Log
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.database.entity.toEntity
import cn.bit101.android.manager.base.CourseScheduleSettingManager
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.net.base.APIManager
import cn.bit101.android.repo.base.CoursesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DefaultCoursesRepo @Inject constructor(
    private val database: BIT101Database,
    private val apiManager: APIManager,
    private val loginStatusManager: LoginStatusManager,
    private val courseScheduleSettingManager: CourseScheduleSettingManager
) : CoursesRepo {
    private val api = apiManager.api

    override suspend fun getCoursesFromNet(
        term: String
    ) = withContext(Dispatchers.IO) {
        api.schoolJxzxehallapp.getAppConfig()
        api.schoolJxzxehallapp.switchLang()

        if(term.isBlank()) throw Exception("term is empty error")

        val courseList = api.schoolJxzxehallapp.getSchedule(term).body()?.datas?.cxxszhxqkb?.rows
            ?: throw Exception("get course list error")

        courseList.map { it.toEntity() }
    }

    override suspend fun saveCourses(
        courses: List<CourseScheduleEntity>
    ) = withContext(Dispatchers.IO) {
        // 删掉所有课程
        database.coursesDao().deleteAllCourses()

        // 放入进现在获得的所有课程
        database.coursesDao().insertCourses(courses)
    }

    override fun getCoursesFromLocal(
        term: String,
    ) = database.coursesDao().getCoursesByTerm(term)

    override fun getCoursesFromLocal(
        term: String,
        week: Int
    ) = database.coursesDao().getCoursesByTermWeek(term, week)

    override fun getCoursesFromLocal() = database.coursesDao().getAllCourses()

    override suspend fun getTermListFromNet() = withContext(Dispatchers.IO) {
        api.schoolJxzxehallapp.getAppConfig()
        val terms = api.schoolJxzxehallapp.getTerms().body()?.datas?.xnxqcx?.rows?.map { it.DM }
            ?: throw Exception("Get Term List Error")
        Log.i("SchoolSchedule", "Get Term List Success: $terms")
        terms
    }

    override suspend fun getCurrentTermFromNet() = withContext(Dispatchers.IO) {
        api.schoolJxzxehallapp.getAppConfig()

        api.schoolJxzxehallapp.getCurrentTerm().body()?.datas?.dqxnxq?.rows?.get(0)?.DM
            ?: throw Exception("get current term error")
    }

    override fun getCurrentTermFromLocal() = courseScheduleSettingManager.term.flow

    override suspend fun getFirstDayFromNet(
        term: String
    ) = withContext(Dispatchers.IO) {
        api.schoolJxzxehallapp.getAppConfig()

        val res = api.schoolJxzxehallapp.getWeekAndDate(
            requestParamStr = "{\"XNXQDM\":\"$term\",\"ZC\":\"1\"}"
        )

        val data = res.body()?.data ?: throw Exception("get first day response error")

        var firstDay: LocalDate? = null

        data.forEach {
            if(it.week == 1) {
                firstDay = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                return@forEach
            }
        }

        firstDay!!
    }

    override fun getFirstDayFromLocal() =
        courseScheduleSettingManager.firstDay.flow
}