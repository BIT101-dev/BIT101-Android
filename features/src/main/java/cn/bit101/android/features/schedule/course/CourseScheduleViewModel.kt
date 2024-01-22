package cn.bit101.android.features.schedule.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.features.common.SimpleState
import cn.bit101.android.features.common.withScope
import cn.bit101.android.features.common.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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
    private val courseScheduleSettings: CourseScheduleSettings
) : ViewModel() {
    private val _courses = MutableStateFlow<List<List<CourseScheduleEntity>>>(emptyList())
    val courses: StateFlow<List<List<CourseScheduleEntity>>> = _courses.asStateFlow()

    val firstDayFlow = courseScheduleSettings.firstDay.flow

    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()


    // 课表相关信息
    val currentTermFlow = courseScheduleSettings.term.flow
    val timeTableStringFlow = courseScheduleSettings.timeTable.flow

    private val coursesFlow = coursesRepo.getCoursesFromLocal()


    // 显示相关配置
    val showSaturdayFlow = courseScheduleSettings.showSaturday.flow
    val showSundayFlow = courseScheduleSettings.showSunday.flow
    val showBorderFlow = courseScheduleSettings.showBorder.flow
    val showHighlightTodayFlow = courseScheduleSettings.highlightToday.flow
    val showDividerFlow = courseScheduleSettings.showDivider.flow
    val showCurrentTimeFlow = courseScheduleSettings.showCurrentTime.flow

    val refreshCoursesStateLiveData = MutableLiveData<SimpleState?>(null)
    val forceRefreshCoursesStateLiveData = MutableLiveData<SimpleState?>(null)

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
        courseScheduleSettings.term.set(term)

        // 获取课表
        val courses = coursesRepo.getCoursesFromNet(term)
        coursesRepo.saveCourses(courses)

        // 获取学期第一天
        val firstDay = coursesRepo.getFirstDayFromNet(term)
        courseScheduleSettings.firstDay.set(firstDay)
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