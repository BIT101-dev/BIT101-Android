package cn.bit101.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.App
import cn.bit101.android.database.CourseScheduleEntity
import cn.bit101.android.database.DataStore
import cn.bit101.android.net.school.CourseResponseItem
import cn.bit101.android.net.school.getCourseSchedule
import cn.bit101.android.net.school.getTermList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * @author flwfdd
 * @date 2023/3/31 23:30
 * @description _(:з」∠)_
 */
class ScheduleViewModel : ViewModel() {
    private val _courses = MutableStateFlow<List<List<CourseScheduleEntity>>>(emptyList())
    val courses: StateFlow<List<List<CourseScheduleEntity>>> = _courses.asStateFlow()
    val termFlow = DataStore.courseScheduleTermFlow
    private var _firstDayFlow = MutableStateFlow<LocalDate?>(null)
    val firstDayFlow = _firstDayFlow.asStateFlow()
    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()
    val timeTableStringFlow = DataStore.courseScheduleTimeTableFlow
    val timeTableFlow = timeTableStringFlow.map { parseTimeTable(it) }

    // 显示相关配置
    val showSaturday = DataStore.courseScheduleShowSaturdayFlow
    val showSunday = DataStore.courseScheduleShowSundayFlow
    val showBorder = DataStore.courseScheduleShowBorderFlow
    val showHighlightToday = DataStore.courseScheduleShowHighlightTodayFlow
    val showDivider = DataStore.courseScheduleShowDividerFlow
    val loginStatus = DataStore.loginStatusFlow

    private var job: Job? = null

    init {
        // 更新学期第一天
        viewModelScope.launch {
            termFlow.collect {
                if (it == null) return@collect
                _firstDayFlow.value = DataStore.getCourseScheduleFirstDayFlow(it).firstOrNull()
            }
        }

        // 移动到当前周
        viewModelScope.launch {
            firstDayFlow.collect {
                changeWeek(it?.until(LocalDate.now(), ChronoUnit.WEEKS)?.plus(1)?.toInt() ?: 1)
            }
        }
    }

    fun changeWeek(week: Int) {
        _weekFlow.value = week
        job?.cancel()
        job = viewModelScope.launch {
            termFlow.collect { term ->
                App.DB.courseScheduleDao().getWeek(term ?: "", week).collect {
                    _courses.value = convertWeekCourse(it)
                }
            }
        }
    }

    fun changeTerm(term: String, onSuccess: () -> Unit = {}, onFail: () -> Unit = {}) {
        viewModelScope.launch {
            val courses = App.DB.courseScheduleDao().getTerm(term).firstOrNull()
            if (courses?.isNotEmpty() == true) {
                DataStore.setString(DataStore.COURSE_SCHEDULE_TERM, term)
                onSuccess()
            } else {
                if (getCoursesFromNet(term)) onSuccess() else onFail()
            }
        }
    }

    fun setShowSaturday(show: Boolean) {
        DataStore.setBoolean(DataStore.COURSE_SCHEDULE_SHOW_SATURDAY, show)
    }

    fun setShowSunday(show: Boolean) {
        DataStore.setBoolean(DataStore.COURSE_SCHEDULE_SHOW_SUNDAY, show)
    }

    fun setShowBorder(show: Boolean) {
        DataStore.setBoolean(DataStore.COURSE_SCHEDULE_SHOW_BORDER, show)
    }

    fun setShowHighlightToday(show: Boolean) {
        DataStore.setBoolean(DataStore.COURSE_SCHEDULE_SHOW_HIGHLIGHT_TODAY, show)
    }

    fun setShowDivider(show: Boolean) {
        DataStore.setBoolean(DataStore.COURSE_SCHEDULE_SHOW_DIVIDER, show)
    }

    fun setTimeTable(timeTable: String) {
        DataStore.setString(DataStore.COURSE_SCHEDULE_TIME_TABLE, timeTable)
    }
}

// 将课程按照周一到周日分组排列
fun convertWeekCourse(_courses: List<CourseScheduleEntity>): List<List<CourseScheduleEntity>> {
    val courses = _courses.sortedBy { it.start_section }
    val weekCourses = mutableListOf<List<CourseScheduleEntity>>()
    for (i in 1..7) {
        val dayCourses = mutableListOf<CourseScheduleEntity>()
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
suspend fun getCoursesFromNet(term: String = ""): Boolean {
    val courses = getCourseSchedule(term) ?: return false
    App.DB.courseScheduleDao().deleteTerm(courses.term) // 删除旧的课程表
    DataStore.setString(DataStore.COURSE_SCHEDULE_TERM, courses.term)
    DataStore.setCourseScheduleFirstDay(courses.term,courses.firstDay)
    courses.courseList.forEach {
        App.DB.courseScheduleDao().insert(course2db(it))
    }
    return true
}

// 将网络请求返回的课程转换为数据库实体
fun course2db(it: CourseResponseItem): CourseScheduleEntity {
    var weeks = ""
    it.SKZC?.forEachIndexed { index, c ->
        if (c == '1') {
            weeks += "[${index + 1}]"
        }
    }
    return CourseScheduleEntity(
        0,
        it.XNXQDM ?: "",
        it.KCM ?: "",
        it.SKJS ?: "",
        it.JASMC ?: "",
        it.YPSJDD ?: "",
        weeks,
        it.SKXQ ?: 0,
        it.KSJC ?: 0,
        it.JSJC ?: 0,
        it.XXXQMC ?: "",
        it.KCH ?: "",
        it.XF ?: 0,
        it.XS ?: 0,
        it.KCXZDM_DISPLAY ?: "",
        it.KCLBDM_DISPLAY ?: "",
        it.KKDWDM_DISPLAY ?: ""
    )
}

// 获取学期列表
suspend fun getTermsFromNet(): List<String> {
    return getTermList().map { it.DM }
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