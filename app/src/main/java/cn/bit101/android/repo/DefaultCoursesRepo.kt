package cn.bit101.android.repo

import android.util.Log
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.database.entity.toEntity
import cn.bit101.android.datastore.SettingDataStore
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
        term: String,
        courses: List<CourseScheduleEntity>
    ) = withContext(Dispatchers.IO) {
        // 删掉这个学期的所有课程
        database.coursesDao().deleteCoursesByTerm(term)

        // 放入进现在获得的所有课程
        database.coursesDao().insertCourses(courses)
    }

    override suspend fun getCoursesFromLocal(
        term: String,
    ) = withContext(Dispatchers.IO) {
        database.coursesDao().getCoursesByTerm(term).first()
    }

    override suspend fun getCoursesFromLocal(
        term: String,
        week: Int
    ) = withContext(Dispatchers.IO) {
        database.coursesDao().getCoursesByTermWeek(term, week).first()
    }

    override suspend fun getTermList() = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getAppConfig()
        val terms = BIT101API.schoolJxzxehallapp.getTerms().body()?.datas?.xnxqcx?.rows?.map { it.DM }
            ?: throw Exception("Get Term List Error")
        Log.i("SchoolSchedule", "Get Term List Success: $terms")
        terms
    }

    override suspend fun getCurrentTerm() = withContext(Dispatchers.IO) {
        val terms = getTermList()
        terms.forEach {
            val firstDay = SettingDataStore.courseScheduleFirstDay.get(it) ?: getFirstDayFromNet(it)
            if(firstDay < LocalDate.now()) {
                return@withContext it
            }
        }
        throw Exception("get current term error")
    }

    override suspend fun getFirstDayFromNet(
        term: String
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

        firstDay!!
    }
}