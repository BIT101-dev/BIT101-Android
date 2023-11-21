package cn.bit101.android.ui.schedule.course

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val database: BIT101Database,
) : ViewModel() {
    private val _courses = MutableStateFlow<List<List<CourseScheduleEntity>>>(emptyList())
    val courses: StateFlow<List<List<CourseScheduleEntity>>> = _courses.asStateFlow()

    val firstDayFlow = SettingDataStore.courseScheduleFirstDay.getFlow("")

    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()


    // 课表相关信息
    val currentTermFlow = SettingDataStore.courseScheduleTerm.flow
    val timeTableStringFlow = SettingDataStore.courseScheduleTimeTable.flow

    private val coursesFlow = coursesRepo.getCoursesFromLocal()


    // 显示相关配置
    val showSaturdayFlow = SettingDataStore.courseScheduleShowSaturday.flow
    val showSundayFlow = SettingDataStore.courseScheduleShowSunday.flow
    val showBorderFlow = SettingDataStore.courseScheduleShowBorder.flow
    val showHighlightTodayFlow = SettingDataStore.courseScheduleShowHighlightToday.flow
    val showDividerFlow = SettingDataStore.courseScheduleShowDivider.flow
    val showCurrentTimeFlow = SettingDataStore.courseScheduleShowCurrentTime.flow

    val refreshCoursesStateLiveData = MutableLiveData<SimpleState>(null)
    val forceRefreshCoursesStateLiveData = MutableLiveData<SimpleState>(null)

    init {
        // 课表、周数改变
        viewModelScope.launch(Dispatchers.IO) {
            combine(coursesFlow, weekFlow, currentTermFlow) { courses, week, term ->
                val weekCourses = convertWeekCourse(
                    courses.filter {  course ->
                        course.weeks.contains("[$week]") && course.term == term
                    }
                )
                _courses.value = weekCourses
            }.collect()
        }

        // 学期开始日期改变
        viewModelScope.launch(Dispatchers.IO) {
            firstDayFlow.collect { firstDay ->
                _weekFlow.value = firstDay?.until(LocalDate.now(), ChronoUnit.WEEKS)?.plus(1)?.toInt() ?: Int.MAX_VALUE
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
        _weekFlow.value = week
    }

    /**
     * 强制刷新课表，需要从网络中重新获取课表
     */
    fun forceRefreshCourses() {
        forceRefreshCoursesStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 获得学期
                val term = coursesRepo.getCurrentTermFromNet()
                SettingDataStore.courseScheduleTerm.set(term)

                // 获取课表
                val courses = coursesRepo.getCoursesFromNet(term)
                coursesRepo.saveCourses(courses)

                // 获取学期第一天
                val firstDay = coursesRepo.getFirstDayFromNet(term)
                SettingDataStore.courseScheduleFirstDay.set("", firstDay)
                forceRefreshCoursesStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                forceRefreshCoursesStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }


    // 将课程按照周一到周日分组排列
    private fun convertWeekCourse(_courses: List<CourseScheduleEntity>): List<List<CourseScheduleEntity>> {
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
}