package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.toTimeTable
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleDataStateLiveData
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class SettingData(
    val showDivider: Boolean,
    val showSaturday: Boolean,
    val showSunday: Boolean,
    val showHighlightToday: Boolean,
    val showBorder: Boolean,
    val showCurrentTime: Boolean,
) {
    companion object {
        val default = SettingData(
            showDivider = false,
            showSaturday = false,
            showSunday = false,
            showHighlightToday = false,
            showBorder = false,
            showCurrentTime = false
        )
    }
}

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    private val coursesRepo: CoursesRepo,
    private val courseScheduleSettings: CourseScheduleSettings
) : ViewModel() {

    // 设置的当前学期
    val currentTermFlow = coursesRepo.getCurrentTermFromLocal()

    // 当前学期的第一天
    val firstDayFlow = courseScheduleSettings.firstDay.flow

    val settingDataFlow = combine(
        courseScheduleSettings.showSaturday.flow,
        courseScheduleSettings.showSunday.flow,
        courseScheduleSettings.showBorder.flow,
        courseScheduleSettings.highlightToday.flow,
        courseScheduleSettings.showDivider.flow,
        courseScheduleSettings.showCurrentTime.flow
    ) { settings ->
        SettingData(
            showSaturday = settings[0],
            showSunday = settings[1],
            showBorder = settings[2],
            showHighlightToday = settings[3],
            showDivider = settings[4],
            showCurrentTime = settings[5]
        )
    }

    val timeTableFlow = courseScheduleSettings.timeTable.flow

    // 学期列表获取状态
    val getTermListStateLiveData = MutableLiveData<SimpleDataState<List<String>>?>(null)

    // 学期起始日期获取状态
    val getFirstDayStateLiveData = MutableLiveData<SimpleState?>(null)

    // 课程获取状态
    val getCoursesStateLiveData = MutableLiveData<SimpleState?>(null)

    // 设置当前学期的状态
    val setCurrentTermStateLiveData = MutableLiveData<SimpleState?>(null)

    // 设置时间表的状态
    val setTimeTableStateLiveData = MutableLiveData<SimpleState?>(null)

    fun getTermList() = withSimpleDataStateLiveData(getTermListStateLiveData) {
        coursesRepo.getTermListFromNet()
    }

    fun setCurrentTerm(term: String) {
        setCurrentTermStateLiveData.value = SimpleState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val oldTerm = currentTermFlow.first() ?: ""

            try {
                courseScheduleSettings.term.set(term)

                // 重新获取第一天
                getFirstDayWithoutState()

                // 重新获取课程
                getCoursesWithoutState()

                setCurrentTermStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                try {
                    courseScheduleSettings.term.set(oldTerm)

                    // 重新获取第一天
                    getFirstDayWithoutState()

                    // 重新获取课程
                    getCoursesWithoutState()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                e.printStackTrace()
                setCurrentTermStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }


    fun setSettingData(settingData: SettingData) = withScope {
        courseScheduleSettings.showDivider.set(settingData.showDivider)
        courseScheduleSettings.showSaturday.set(settingData.showSaturday)
        courseScheduleSettings.showSunday.set(settingData.showSunday)
        courseScheduleSettings.highlightToday.set(settingData.showHighlightToday)
        courseScheduleSettings.showBorder.set(settingData.showBorder)
        courseScheduleSettings.showCurrentTime.set(settingData.showCurrentTime)
    }

    private suspend fun getFirstDayWithoutState() {
        val term = currentTermFlow.first() ?: throw Exception("no term")
        val firstDay = coursesRepo.getFirstDayFromNet(term)
        courseScheduleSettings.firstDay.set(firstDay)
    }

    fun getFirstDay() = withSimpleStateLiveData(getFirstDayStateLiveData) {
        getFirstDayWithoutState()
    }

    private suspend fun getCoursesWithoutState() {
        val term = currentTermFlow.first() ?: throw Exception("no term")
        val courses = coursesRepo.getCoursesFromNet(term)
        coursesRepo.saveCourses(courses)
    }

    fun getCourses() = withSimpleStateLiveData(getCoursesStateLiveData) {
        getCoursesWithoutState()
    }

    fun setTimeTable(timeTableStr: String) = withSimpleStateLiveData(setTimeTableStateLiveData) {
        courseScheduleSettings.timeTable.set(timeTableStr.toTimeTable())
    }
}