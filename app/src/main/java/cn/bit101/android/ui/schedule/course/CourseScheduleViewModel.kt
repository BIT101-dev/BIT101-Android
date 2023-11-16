package cn.bit101.android.ui.schedule.course

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.database.BIT101Database
import cn.bit101.android.database.entity.CourseEntity
import cn.bit101.android.database.entity.toEntity
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.datastore.UserDataStore
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
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

data class TermWeekCoursesData(
    val term: String,
    val week: Int,
    val courses: List<CourseEntity>,
)

@HiltViewModel
class CourseScheduleViewModel @Inject constructor(
    private val coursesRepo: CoursesRepo,
    private val database: BIT101Database,
) : ViewModel() {
    private val _courses = MutableStateFlow<List<List<CourseEntity>>>(emptyList())
    val courses: StateFlow<List<List<CourseEntity>>> = _courses.asStateFlow()

    private val _firstDayFlow = MutableStateFlow<LocalDate?>(null)
    val firstDayFlow = _firstDayFlow.asStateFlow()
    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()

    val coursesFlow = database.coursesDao().getAllCourses()


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

    val setTimeTableStateLiveData = MutableLiveData<SimpleState>(null)
    val forceRefreshCoursesStateLiveData = MutableLiveData<SimpleState>(null)
    val refreshTermListStateLiveData = MutableLiveData<SimpleDataState<List<String>>>(null)
    val changeTermStateLiveData = MutableLiveData<SimpleState>(null)

    private var job: Job? = null

    init {
        // 为第一次打开做准备
        // 第一次打开时，term、week、firstDay、课表都是空的，这里用term来判断是否第一次打开
        firstLaunch()

        // 如果学期变化，则更新学期第一天，同时获取这学期所有课程
        viewModelScope.launch {
            termFlow.collect {
                if (it.isEmpty()) return@collect
                try {
                    coursesRepo.getCoursesFromNet(it)

                    val firstDay = SettingDataStore.courseScheduleFirstDay.get(it) ?: coursesRepo.getFirstDayFromNet(it)

                    _firstDayFlow.value = firstDay
                } catch (e: Exception) {
                    SettingDataStore.courseScheduleTerm.remove()
                    _firstDayFlow.value = null
                    firstLaunch()
                }
            }
        }
        // 如果学期第一天变化，则更新周
        viewModelScope.launch(Dispatchers.IO) {
            firstDayFlow.collect {
                if (it == null) return@collect
                val week = it.until(LocalDate.now(), ChronoUnit.WEEKS).plus(1).toInt()
                _weekFlow.value = week
            }
        }

        // 如果学期、周或者课表变化，则更新这学期的课表
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                termFlow,
                weekFlow,
                coursesFlow,
            ) { term, week, courses ->
                TermWeekCoursesData(term, week, courses)
            }.collect { data ->
                val term = data.term
                val week = data.week
                val weekCourses = coursesRepo.getCoursesFromLocal(term, week)
                _courses.value = convertWeekCourse(weekCourses)
            }
        }
    }

    private fun firstLaunch() {
        viewModelScope.launch(Dispatchers.IO) {
            val term = termFlow.firstOrNull()
            if(!term.isNullOrEmpty()) return@launch

            // 第一次打开
            // 先获取学期列表
            val terms = coursesRepo.getTermListFromNet(true)
            terms.forEach {
                // 获取学期第一天
                try {
                    coursesRepo.getFirstDayFromNet(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            terms.forEach {
                // 获取学期第一天
                val day = try {
                    coursesRepo.getFirstDayFromLocal(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@forEach
                }

                if(day < LocalDate.now()) {
                    // 如果学期第一天在今天之前，就是当前学期

                    // 设置学期
                    SettingDataStore.courseScheduleTerm.set(it)

                    return@launch
                }
            }
        }
    }

    fun changeWeek(week: Int) {
        _weekFlow.value = week
    }

    fun changeTerm(term: String) {
        changeTermStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SettingDataStore.courseScheduleTerm.set(term)
                changeTermStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                changeTermStateLiveData.postValue(SimpleState.Error)
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
        setTimeTableStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            if(!checkTimeTable(timeTable)) {
                setTimeTableStateLiveData.postValue(SimpleState.Error)
                return@launch
            }
            SettingDataStore.courseScheduleTimeTable.set(timeTable)
            setTimeTableStateLiveData.postValue(SimpleState.Success)
        }
    }

    // 将课程按照周一到周日分组排列
    private fun convertWeekCourse(_courses: List<CourseEntity>): List<List<CourseEntity>> {
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


    fun getCoursesFromNet() {
        forceRefreshCoursesStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                coursesRepo.getCoursesFromNet()
                forceRefreshCoursesStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                forceRefreshCoursesStateLiveData.postValue(SimpleState.Error)
            }
        }
    }

    // 获取学期列表
    fun getTermsFromNet() {
        refreshTermListStateLiveData.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val terms = coursesRepo.getTermListFromNet()
                refreshTermListStateLiveData.postValue(SimpleDataState.Success(terms))
            } catch (e: Exception) {
                refreshTermListStateLiveData.postValue(SimpleDataState.Error())
            }
        }
    }

    // 解析时间表
    data class TimeTableItem(
        val startTime: LocalTime,
        val endTime: LocalTime,
    )

    private fun checkTimeTable(timeTable: String): Boolean {
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


    private fun parseTimeTable(s: String): List<TimeTableItem> {
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