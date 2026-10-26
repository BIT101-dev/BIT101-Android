package cn.bit101.android.data.repo

import androidx.room.withTransaction
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.database.BIT101Database
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import cn.bit101.android.data.database.entity.toEntity
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.api.model.common.SchoolCookie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

internal class DefaultCoursesRepo @Inject constructor(
    private val database: BIT101Database,
    private val apiManager: APIManager,
    private val courseScheduleSettings: CourseScheduleSettings,
    private val loginStatus: LoginStatus,
) : CoursesRepo {
    private suspend fun schoolCookies(): List<SchoolCookie> =
        SchoolCookieStore.snapshot(loginStatus.cookieManager.cookieStore)

    private suspend fun webVpn(): Boolean = loginStatus.webVpn.get()

    override suspend fun getCoursesFromNet(
        term: String
    ) = withContext(Dispatchers.IO) {
        if(term.isBlank()) throw Exception("term is empty error")

        logApiCall(TAG, "获取课程表(term=$term)", result = { "成功, 共 ${it.size} 条课程" }) {
            apiManager.api.schoolJxzxehall.getCourseSchedule(
                cookies = schoolCookies(),
                webVpn = webVpn(),
                term = term,
            ).map { it.toEntity() }
        }
    }

    override suspend fun getExamsFromNet(
        term: String
    ) = withContext(Dispatchers.IO) {
        if(term.isBlank()) throw Exception("term is empty error")

        logApiCall(TAG, "获取考试安排(term=$term)", result = { "成功, 共 ${it.size} 场考试" }) {
            apiManager.api.schoolJxzxehall.getExamList(
                cookies = schoolCookies(),
                webVpn = webVpn(),
                term = term,
            ).map { it.toEntity() }
        }
    }

    override suspend fun saveSchedule(
        courses: List<CourseScheduleEntity>?,
        exams: List<ExamScheduleEntity>?,
        term: String?,
        firstDay: LocalDate?,
    ) = database.withTransaction {
        courses?.let {
            database.coursesDao().deleteAllCourses()
            database.coursesDao().insertCourses(it)
        }
        exams?.let {
            database.examsDao().deleteAllExams()
            database.examsDao().insertExams(it)
        }
        // 学期设置写入失败时，数据库替换也回滚；网络失败从不进入提交阶段。
        if (term != null) courseScheduleSettings.setTerm(term, firstDay)
        Unit
    }

    override fun getCoursesFromLocal(
        term: String,
    ) = database.coursesDao().getCoursesByTerm(term).forSignedInAccount()

    override fun getCoursesFromLocal(
        term: String,
        week: Int
    ) = database.coursesDao().getCoursesByTermWeek(term, week).forSignedInAccount()

    override fun getCoursesFromLocal() = database.coursesDao().getAllCourses().forSignedInAccount()

    override fun getExamsFromLocal(
        term: String,
    ) = database.examsDao().getExamsByTerm(term).forSignedInAccount()

    override fun getExamsFromLocal() = database.examsDao().getAllExams().forSignedInAccount()

    private fun <T> Flow<List<T>>.forSignedInAccount(): Flow<List<T>> =
        combine(loginStatus.sid.flow) { records, sid -> if (sid.isBlank()) emptyList() else records }

    override suspend fun addCustomSchedule(
        schedule: CustomScheduleEntity,
    ) = withContext(Dispatchers.IO) {
        database.customScheduleDao().insertSchedule(schedule)
    }

    override fun getCustomSchedules() = database.customScheduleDao().getAllSchedules()

    override suspend fun deleteCustomSchedule(scheduleEntity: CustomScheduleEntity) = withContext(Dispatchers.IO) {
        database.customScheduleDao().deleteSchedule(scheduleEntity)
    }

    override suspend fun updateCustomSchedule(scheduleEntity: CustomScheduleEntity) = withContext(Dispatchers.IO) {
        database.customScheduleDao().updateSchedule(scheduleEntity)
    }

    override suspend fun getTermListFromNet() = withContext(Dispatchers.IO) {
        logApiCall(TAG, "获取学期列表", result = { "成功: $it" }) {
            apiManager.api.schoolJxzxehall.getTermList(
                cookies = schoolCookies(),
                webVpn = webVpn(),
            )
        }
    }

    override suspend fun getCurrentTermFromNet() = withContext(Dispatchers.IO) {
        logApiCall(TAG, "获取当前学期", result = { "成功: $it" }) {
            apiManager.api.schoolJxzxehall.getCurrentTerm(
                cookies = schoolCookies(),
                webVpn = webVpn(),
            )
        }
    }

    override fun getCurrentTermFromLocal() = courseScheduleSettings.term.flow

    override suspend fun getFirstDayFromNet(
        term: String
    ) = withContext(Dispatchers.IO) {
        val firstDay = logApiCall(TAG, "获取学期开始日期(term=$term)", result = { "成功: $it" }) {
            apiManager.api.schoolJxzxehall.getFirstDay(
                cookies = schoolCookies(),
                webVpn = webVpn(),
                term = term,
            )
        }
        LocalDate.parse(firstDay)
    }

    override fun getFirstDayFromLocal() =
        courseScheduleSettings.firstDay.flow

    private companion object {
        const val TAG = "CoursesRepo"
    }
}
