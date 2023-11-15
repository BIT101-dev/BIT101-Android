package cn.bit101.android.ui.schedule.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.CourseEntity
import cn.bit101.android.database.entity.toEntity
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.CourseForSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.fortuna.ical4j.model.DateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * @author flwfdd
 * @date 2023/3/31 23:30
 * @description _(:з」∠)_
 */
@HiltViewModel
class CourseScheduleViewModel @Inject constructor(
    private val coursesRepo: CoursesRepo,
    private val database: BIT101Database,
) : ViewModel() {
    private val _courses = MutableStateFlow<List<List<CourseEntity>>>(emptyList())
    val courses: StateFlow<List<List<CourseEntity>>> = _courses.asStateFlow()

    private var _firstDayFlow = MutableStateFlow<LocalDate?>(null)
    val firstDayFlow = _firstDayFlow.asStateFlow()
    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()


    // 课表相关信息
    val termFlow = SettingDataStore.courseScheduleTerm.flow
    val timeTableStringFlow = SettingDataStore.courseScheduleTimeTable.flow
    val timeTableFlow = timeTableStringFlow.map { parseTimeTable(it) }


    // 显示相关配置
    val showSaturdayFlow = SettingDataStore.courseScheduleShowSaturday.flow
    val showSundayFlow = SettingDataStore.courseScheduleShowSunday.flow
    val showBorderFlow = SettingDataStore.courseScheduleShowBorder.flow
    val showHighlightTodayFlow = SettingDataStore.courseScheduleShowHighlightToday.flow
    val showDividerFlow = SettingDataStore.courseScheduleShowDivider.flow
    val showCurrentTimeFlow = SettingDataStore.courseScheduleShowCurrentTime.flow

    private var job: Job? = null

    init {
        // 更新学期第一天
        viewModelScope.launch {
            termFlow.collect {
                if (it.isEmpty()) return@collect
                _firstDayFlow.value = SettingDataStore.courseScheduleFirstDay.get(it)
            }
        }

        // 移动到当前周
        viewModelScope.launch {
            firstDayFlow.collect {
                changeWeek(it?.until(LocalDate.now(), ChronoUnit.WEEKS)?.plus(1)?.toInt() ?: 1)
            }
        }

        viewModelScope.launch {
            database.coursesDao().getAllCourses().collect {
                Log.i("ScheduleGetAll", it.toString())
            }
        }

        viewModelScope.launch {
            val terms = coursesRepo.getTermList(true)
            terms.forEach {
                val day = coursesRepo.getFirstDay(
                    term = it,
                    forceNet = true,
                )
                if(day < LocalDate.now()) {
                    SettingDataStore.courseScheduleTerm.set(it)
                    SettingDataStore.courseScheduleFirstDay.set(it, day)
                    coursesRepo.getCourses(
                        term = it,
                        forceNet = true,
                    )
                    return@launch
                }
            }
        }
    }

    fun changeWeek(week: Int) {
        _weekFlow.value = week
        job?.cancel()
        job = viewModelScope.launch {
            termFlow.collect { term ->
                Log.i("ScheduleTermWeek", "$term, $week")
                database.coursesDao().getCoursesByTermWeek(term, week).collect {
                    _courses.value = convertWeekCourse(it)
                    Log.i("Schedule", it.toString())
                    Log.i("Schedule", convertWeekCourse(it).toString())
                }
            }
        }
    }

    fun changeTerm(term: String, onSuccess: () -> Unit = {}, onFail: () -> Unit = {}) {
        viewModelScope.launch {
            val courses = database.coursesDao().getCoursesByTerm(term).firstOrNull()
            if (courses?.isNotEmpty() == true) {
                SettingDataStore.courseScheduleTerm.set(term)
                onSuccess()
            } else {
                if (getCoursesFromNet(term)) onSuccess() else onFail()
            }
        }
    }

    fun setShowSaturday(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleShowSaturday.set(show)
        }
    }
    fun setShowSunday(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleShowSunday.set(show)
        }
    }
    fun setShowBorder(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleShowBorder.set(show)
        }
    }
    fun setShowHighlightToday(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleShowHighlightToday.set(show)
        }
    }
    fun setShowDivider(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleShowDivider.set(show)
        }
    }
    fun setShowCurrentTime(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleShowCurrentTime.set(show)
        }
    }
    fun setTimeTable(timeTable: String) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleTimeTable.set(timeTable)
        }
    }


    // 将课程按照周一到周日分组排列
    fun convertWeekCourse(_courses: List<CourseEntity>): List<List<CourseEntity>> {
        val courses = _courses.sortedBy { it.start_section }
        val weekCourses = mutableListOf<List<CourseEntity>>()
        for (i in 1..7) {
            val dayCourses = mutableListOf<CourseEntity>()
            for (course in courses) {
                if (course.weekday == i) {
                    dayCourses.add(course)
                }
            }
            weekCourses.add(dayCourses)
        }
        return weekCourses
    }


    // 获取课程表 返回是否成功
    suspend fun getCoursesFromNet(term: String = "") = withContext(Dispatchers.IO) {
        try {
            coursesRepo.getCourses(
                term = term,
                forceNet = true,
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    // 获取学期列表
    suspend fun getTermsFromNet() = withContext(Dispatchers.IO) {
        BIT101API.schoolJxzxehallapp.getTerms().body()?.datas?.xnxqcx?.rows?.let { trems ->
            trems.map { it.DM }
        } ?: emptyList()
    }

    // 解析时间表
    data class TimeTableItem(
        val startTime: LocalTime,
        val endTime: LocalTime,
    )

    fun checkTimeTable(timeTable: String): Boolean {
        return try {
            val l = parseTimeTable(timeTable)
            if (l.isEmpty()) return false
            l.forEachIndexed { index, time ->
                if (time.endTime <= time.startTime) return false
                if (index != 0) {
                    if (time.startTime <= l[index - 1].endTime) return false
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }


    fun parseTimeTable(s: String): List<TimeTableItem> {
        val timeTable = mutableListOf<TimeTableItem>()

        s.split("\n").forEach {
            if (it.isBlank()) return@forEach
            val x = it.split(",")
            timeTable.add(
                TimeTableItem(
                    LocalTime.parse(x[0]),
                    LocalTime.parse(x[1])
                )
            )
        }
        return timeTable
    }
}