package cn.bit101.android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.App
import cn.bit101.android.database.CourseScheduleEntity
import cn.bit101.android.database.DataStore
import cn.bit101.android.net.school.CourseResponseItem
import cn.bit101.android.net.school.getCourseSchedule
import cn.bit101.android.net.school.getTermList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * @author flwfdd
 * @date 2023/3/31 23:30
 * @description _(:з」∠)_
 */

const val TAG = "ScheduleViewModel"

class ScheduleViewModel : ViewModel() {
    private val _courses = MutableStateFlow<List<List<CourseScheduleEntity>>>(emptyList())
    val courses: StateFlow<List<List<CourseScheduleEntity>>> = _courses.asStateFlow()
    val termFlow = DataStore.courseScheduleTermFlow
    val firstDayFlow = DataStore.courseScheduleFirstDayFlow
    private val _weekFlow = MutableStateFlow(1)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()

    // 显示相关配置
    val showSaturday = DataStore.courseScheduleShowSaturdayFlow
    val showSunday = DataStore.courseScheduleShowSundayFlow
    val showBorder = DataStore.courseScheduleShowBorderFlow
    val showHighlightToday = DataStore.courseScheduleShowHighlightTodayFlow
    val showDivider = DataStore.courseScheduleShowDividerFlow

    private var job: Job? = null

    init {
        Log.i(TAG, "init")
        job = viewModelScope.launch {
            App.DB.courseScheduleDao().getAll().cancellable().collect {
                Log.i(TAG, "collect init")
                _courses.value = convertWeekCourse(it)
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
                Log.i(TAG, "collect changeWeek")
                App.DB.courseScheduleDao().getWeek(term ?: "", week).collect {
                    _courses.value = convertWeekCourse(it)
                }
            }
        }
    }

    fun changeTerm(term: String, onSuccess: () -> Unit = {}, onFail: () -> Unit = {}) {
        viewModelScope.launch {
            if (getCoursesFromNet(term)) onSuccess() else onFail()
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
    App.DB.courseScheduleDao().deleteAll()
    val courses = getCourseSchedule(term) ?: return false
    DataStore.setString(DataStore.COURSE_SCHEDULE_TERM, courses.term)
    DataStore.setCourseScheduleFirstDay(courses.firstDay)
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
