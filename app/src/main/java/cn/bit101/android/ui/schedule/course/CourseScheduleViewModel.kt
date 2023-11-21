package cn.bit101.android.ui.schedule.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import cn.bit101.android.utils.TimeTableItem
import cn.bit101.android.utils.TimeTableUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
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

    private val _firstDayFlow = MutableStateFlow<LocalDate?>(null)
    val firstDayFlow = _firstDayFlow.asStateFlow()

    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()


    // 课表相关信息
    val termFlow = SettingDataStore.courseScheduleTerm.flow
    val timeTableStringFlow = SettingDataStore.courseScheduleTimeTable.flow


    // 显示相关配置
    val showSaturdayFlow = SettingDataStore.courseScheduleShowSaturday.flow
    val showSundayFlow = SettingDataStore.courseScheduleShowSunday.flow
    val showBorderFlow = SettingDataStore.courseScheduleShowBorder.flow
    val showHighlightTodayFlow = SettingDataStore.courseScheduleShowHighlightToday.flow
    val showDividerFlow = SettingDataStore.courseScheduleShowDivider.flow
    val showCurrentTimeFlow = SettingDataStore.courseScheduleShowCurrentTime.flow

    val setTimeTableStateLiveData = MutableLiveData<SimpleState>(null)
    val refreshCoursesStateLiveData = MutableLiveData<SimpleState>(null)
    val forceRefreshCoursesStateLiveData = MutableLiveData<SimpleState>(null)
    val refreshTermListStateLiveData = MutableLiveData<SimpleDataState<List<String>>>(null)
    val changeTermStateLiveData = MutableLiveData<SimpleState>(null)

    private var job: Job? = null

    init {
        refreshCourses()
    }

    /**
     * 这里的场景是使用右下角的加减号来改变周数
     * 不需要在网络中获取课表
     * 要考虑到连续点击按钮的情况，之后再说
     */
    fun changeWeek(week: Int) {
        // 改变周数
        _weekFlow.value = week
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 获得学期
                val term = SettingDataStore.courseScheduleTerm.get()

                // 获取课表
                val courses = coursesRepo.getCoursesFromLocal(term, week).first()

                // 将获取的课表放入流中
                _courses.value = convertWeekCourse(courses)
            } catch (e: Exception) {
                _weekFlow.value = week - 1
            }
        }
    }

    /**
     * 在配置界面中选择指定学期进行切换，会出现2021-2022-3这样错误的学期
     * 需要在网络中获取学期的第一天、课表
     */
    fun changeTerm(term: String) {
        changeTermStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 获取学期第一天，如果这里出现了选择了不应该出现的学期，那么就会抛出错误
                val firstDay = SettingDataStore.courseScheduleFirstDay.get(term)
                    ?: coursesRepo.getFirstDayFromNet(term)
                SettingDataStore.courseScheduleFirstDay.set(term, firstDay)
                _firstDayFlow.value = firstDay

                // 获取当前周数
                val week = firstDay.until(LocalDate.now(), ChronoUnit.WEEKS).plus(1).toInt()
                _weekFlow.value = week

                // 获取课表
                val courses = coursesRepo.getCoursesFromLocal(term, week).first().ifEmpty {
                    // 如果为空，那么从网络中获取学期的课表
                    val allCourses = coursesRepo.getCoursesFromNet(term)

                    // 将课表存储到数据库中
                    coursesRepo.saveCourses(term, allCourses)

                    // 最后再获取一遍
                    coursesRepo.getCoursesFromLocal(term, week).first()
                }

                // 将获取的课表放入流中
                _courses.value = convertWeekCourse(courses)

                // 更改学期
                SettingDataStore.courseScheduleTerm.set(term)

                changeTermStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                changeTermStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    /**
     * 在配置界面获取所有的学期列表
     */
    fun refreshTermList() {
        refreshTermListStateLiveData.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val terms = coursesRepo.getTermListFromNet()
                refreshTermListStateLiveData.postValue(SimpleDataState.Success(terms))
            } catch (e: Exception) {
                refreshTermListStateLiveData.postValue(SimpleDataState.Fail())
            }
        }
    }

    /**
     * 强制刷新课表，需要从网络中重新获取课表
     */
    fun forceRefreshCourses() {
        forceRefreshCoursesStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 获得学期
                val term = SettingDataStore.courseScheduleTerm.get().ifBlank {
                    coursesRepo.getCurrentTermFromNet()
                }
                SettingDataStore.courseScheduleTerm.set(term)

                var week = weekFlow.value
                if(week >= Int.MAX_VALUE - 1000) {
                    // 获取学期第一天
                    val firstDay = SettingDataStore.courseScheduleFirstDay.get(term)
                        ?: coursesRepo.getFirstDayFromNet(term)
                    SettingDataStore.courseScheduleFirstDay.set(term, firstDay)
                    _firstDayFlow.value = firstDay

                    // 计算周数
                    week = firstDay.until(LocalDate.now(), ChronoUnit.WEEKS).plus(1).toInt()
                    _weekFlow.value = week
                }

                // 从网络中获取学期的课表
                val allCourses = coursesRepo.getCoursesFromNet(term)

                // 将课表存储到数据库中
                coursesRepo.saveCourses(term, allCourses)

                // 最后再获取一遍
                val courses = coursesRepo.getCoursesFromLocal(term, week).first()
                _courses.value = convertWeekCourse(courses)

                forceRefreshCoursesStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                forceRefreshCoursesStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    /**
     * 从本地获取课程表
     */
    fun refreshCourses() {
        refreshCoursesStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val term = SettingDataStore.courseScheduleTerm.get()
                if(term.isBlank()) throw Exception("term is empty")

                val firstDay = SettingDataStore.courseScheduleFirstDay.get(term)
                    ?: throw Exception("first day is null")
                _firstDayFlow.value = firstDay

                val week = firstDay.until(LocalDate.now(), ChronoUnit.WEEKS).plus(1).toInt()
                _weekFlow.value = week

                val courses = coursesRepo.getCoursesFromLocal(term, week).first()
                _courses.value = convertWeekCourse(courses)

                refreshCoursesStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                refreshCoursesStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    fun setSettingData(settingData: SettingData) {
        viewModelScope.launch(Dispatchers.IO) {
            SettingDataStore.courseScheduleShowBorder.set(settingData.showBorder)
            SettingDataStore.courseScheduleShowDivider.set(settingData.showDivider)
            SettingDataStore.courseScheduleShowSaturday.set(settingData.showSaturday)
            SettingDataStore.courseScheduleShowSunday.set(settingData.showSunday)
            SettingDataStore.courseScheduleShowCurrentTime.set(settingData.showCurrentTime)
            SettingDataStore.courseScheduleShowHighlightToday.set(settingData.showHighlightToday)
        }
    }
    fun setTimeTable(timeTable: String) {
        setTimeTableStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            if(!TimeTableUtils.checkTimeTable(timeTable)) {
                setTimeTableStateLiveData.postValue(SimpleState.Fail)
                return@launch
            }
            SettingDataStore.courseScheduleTimeTable.set(timeTable)
            setTimeTableStateLiveData.postValue(SimpleState.Success)
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