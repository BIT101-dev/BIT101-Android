package cn.bit101.android.repo

import android.util.Log
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.database.entity.toEntity
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.CoursesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DefaultCoursesRepo @Inject constructor(
    private val database: BIT101Database,
) : CoursesRepo {
    override suspend fun getCoursesFromNet(
        term: String
    ) = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getAppConfig()
        BIT101API.schoolJxzxehallapp.switchLang()

        if(term.isBlank()) throw Exception("term is empty error")

        val courseList = BIT101API.schoolJxzxehallapp.getSchedule(term).body()?.datas?.cxxszhxqkb?.rows
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
        BIT101API.schoolJxzxehallapp.getAppConfig()
        val terms = BIT101API.schoolJxzxehallapp.getTerms().body()?.datas?.xnxqcx?.rows?.map { it.DM }
            ?: throw Exception("Get Term List Error")
        Log.i("SchoolSchedule", "Get Term List Success: $terms")
        terms
    }

    override suspend fun getCurrentTermFromNet() = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getAppConfig()

        BIT101API.schoolJxzxehallapp.getCurrentTerm().body()?.datas?.dqxnxq?.rows?.get(0)?.DM
            ?: throw Exception("get current term error")
    }

    override fun getCurrentTermFromLocal() = SettingDataStore.courseScheduleTerm.flow

    override suspend fun getFirstDayFromNet(
        term: String
    ) = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getAppConfig()

        val res = BIT101API.schoolJxzxehallapp.getWeekAndDate(
            requestParamStr = "{\"XNXQDM\":\"$term\",\"ZC\":\"1\"}"
        )

//        Log.i("SchoolSchedule", "Get First Day Response: ${res.code()} ${res.errorBody()?.string()}")


        val data = res.body()?.data ?: throw Exception("get first day response error")

        var firstDay: LocalDate? = null

//        Log.i("SchoolSchedule", "Get First Day Success: $data")

        data.forEach {
            if(it.week == 1) {
                firstDay = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                return@forEach
            }
        }

        firstDay!!
    }

    override fun getFirstDayFromLocal() =
        SettingDataStore.courseScheduleFirstDay.getFlow("")
}