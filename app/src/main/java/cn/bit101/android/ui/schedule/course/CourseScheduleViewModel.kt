package cn.bit101.android.ui.schedule.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.manager.base.CourseScheduleSettingManager
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.withScope
import cn.bit101.android.ui.common.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * @author flwfdd
 * @date 2023/3/31 23:30
 * @description _(:з」∠)_
 */

data class SettingData(
    val showSaturday: Boolean,
    val showSunday: Boolean,
    val showDivider: Boolean,
    val showHighlightToday: Boolean,
    val showBorder: Boolean,
    val showCurrentTime: Boolean,
)

@HiltViewModel
class CourseScheduleViewModel @Inject constructor(
    private val coursesRepo: CoursesRepo,
    private val scheduleSettingManager: CourseScheduleSettingManager
) : ViewModel() {
    private val _courses = MutableStateFlow<List<List<CourseScheduleEntity>>>(emptyList())
    val courses: StateFlow<List<List<CourseScheduleEntity>>> = _courses.asStateFlow()

    val firstDayFlow = scheduleSettingManager.firstDay.flow

    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()


    // 课表相关信息
    val currentTermFlow = scheduleSettingManager.term.flow
    val timeTableStringFlow = scheduleSettingManager.timeTable.flow

    private val coursesFlow = coursesRepo.getCoursesFromLocal()


    // 显示相关配置
    val showSaturdayFlow = scheduleSettingManager.showSaturday.flow
    val showSundayFlow = scheduleSettingManager.showSunday.flow
    val showBorderFlow = scheduleSettingManager.showBorder.flow
    val showHighlightTodayFlow = scheduleSettingManager.highlightToday.flow
    val showDividerFlow = scheduleSettingManager.showDivider.flow
    val showCurrentTimeFlow = scheduleSettingManager.showCurrentTime.flow

    val refreshCoursesStateLiveData = MutableLiveData<SimpleState>(null)
    val forceRefreshCoursesStateLiveData = MutableLiveData<SimpleState>(null)

    init {
        // 课表、周数改变
        withScope {
            combine(coursesFlow, weekFlow) { courses, week ->
                val weekCourses = convertWeekCourse(
                    courses.filter { course ->
                        course.weeks.contains("[$week]")
                    }
                )
                _courses.value = weekCourses
            }.collect()
        }

        // 学期开始日期改变
        withScope {
            firstDayFlow.collect { firstDay ->
                val week = firstDay?.until(LocalDate.now(), ChronoUnit.WEEKS)?.plus(1)?.toInt() ?: Int.MAX_VALUE
                _weekFlow.value = maxOf(week, 1)
            }
        }
    }

    /**
     * 这里的场景是使用右下角的加减号来改变周数
     * 不需要在网络中获取课表
     * 要考虑到连续点击按钮的情况，之后再说
     */
    fun changeWeek(week: Int) {
        // 改变周数
        _weekFlow.value = maxOf(week, 1)
    }

    /**
     * 强制刷新课表，需要从网络中重新获取课表
     */
    fun forceRefreshCourses() = withSimpleStateLiveData(refreshCoursesStateLiveData) {
        // 获得学期
        val term = coursesRepo.getCurrentTermFromNet()
        scheduleSettingManager.term.set(term)

        // 获取课表
        val courses = coursesRepo.getCoursesFromNet(term)
        coursesRepo.saveCourses(courses)

        // 获取学期第一天
        val firstDay = coursesRepo.getFirstDayFromNet(term)
        scheduleSettingManager.firstDay.set(firstDay)
    }


    // 将课程按照周一到周日分组排列
    private fun convertWeekCourse(courses: List<CourseScheduleEntity>): List<List<CourseScheduleEntity>> {
        val finalCourses = courses.sortedBy { it.start_section }
        val weekCourses = mutableListOf<List<CourseScheduleEntity>>()
        for (i in 1..7) {
            val dayCourses = mutableListOf<CourseScheduleEntity>()
            for (course in finalCourses) {
                if (course.weekday == i) {
                    dayCourses.add(course)
                }
            }
            weekCourses.add(dayCourses)
        }
        return weekCourses
    }
}