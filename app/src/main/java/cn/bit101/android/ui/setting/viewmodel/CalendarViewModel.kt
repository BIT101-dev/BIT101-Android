package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.utils.TimeTableUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingData(
    val showDivider: Boolean,
    val showSaturday: Boolean,
    val showSunday: Boolean,
    val showHighlightToday: Boolean,
    val showBorder: Boolean,
    val showCurrentTime: Boolean,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val coursesRepo: CoursesRepo,
) : ViewModel() {

    // 设置的当前学期
    val currentTermFlow = coursesRepo.getCurrentTermFromLocal()

    // 当前学期的第一天
    val firstDayFlow = SettingDataStore.courseScheduleFirstDay.getFlow("")


    // 显示相关配置
    val showSaturdayFlow = SettingDataStore.courseScheduleShowSaturday.flow
    val showSundayFlow = SettingDataStore.courseScheduleShowSunday.flow
    val showBorderFlow = SettingDataStore.courseScheduleShowBorder.flow
    val showHighlightTodayFlow = SettingDataStore.courseScheduleShowHighlightToday.flow
    val showDividerFlow = SettingDataStore.courseScheduleShowDivider.flow
    val showCurrentTimeFlow = SettingDataStore.courseScheduleShowCurrentTime.flow

    val timeTableFlow = SettingDataStore.courseScheduleTimeTable.flow

    // 学期列表获取状态
    val getTermListStateLiveData = MutableLiveData<SimpleDataState<List<String>>>(null)

    // 学期起始日期获取状态
    val getFirstDayStateLiveData = MutableLiveData<SimpleState>(null)

    // 课程获取状态
    val getCoursesStateLiveData = MutableLiveData<SimpleState>(null)

    // 设置当前学期的状态
    val setCurrentTermStateLiveData = MutableLiveData<SimpleState>(null)

    // 设置时间表的状态
    val setTimeTableStateLiveData = MutableLiveData<SimpleState>(null)

    fun getTermList() {
        getTermListStateLiveData.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val termList = coursesRepo.getTermListFromNet()
                getTermListStateLiveData.postValue(SimpleDataState.Success(termList))
            } catch (e: Exception) {
                getTermListStateLiveData.postValue(SimpleDataState.Fail())
            }
        }
    }

    fun setCurrentTerm(term: String) {
        setCurrentTermStateLiveData.value = SimpleState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val oldTerm = currentTermFlow.first() ?: ""

            try {
                SettingDataStore.courseScheduleTerm.set(term)

                // 重新获取第一天
                getFirstDayWithoutState()

                // 重新获取课程
                getCoursesWithoutState()

                setCurrentTermStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                try {
                    SettingDataStore.courseScheduleTerm.set(oldTerm)

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


    fun setSettingData(settingData: SettingData) {
        viewModelScope.launch {
            SettingDataStore.courseScheduleShowDivider.set(settingData.showDivider)
            SettingDataStore.courseScheduleShowSaturday.set(settingData.showSaturday)
            SettingDataStore.courseScheduleShowSunday.set(settingData.showSunday)
            SettingDataStore.courseScheduleShowHighlightToday.set(settingData.showHighlightToday)
            SettingDataStore.courseScheduleShowBorder.set(settingData.showBorder)
            SettingDataStore.courseScheduleShowCurrentTime.set(settingData.showCurrentTime)
        }
    }

    private suspend fun getFirstDayWithoutState() {
        val term = currentTermFlow.first() ?: throw Exception("no term")
        val firstDay = coursesRepo.getFirstDayFromNet(term)
        SettingDataStore.courseScheduleFirstDay.set("", firstDay)
    }

    fun getFirstDay() {
        getFirstDayStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getFirstDayWithoutState()
                getFirstDayStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                getFirstDayStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    private suspend fun getCoursesWithoutState() {
        val term = currentTermFlow.first() ?: throw Exception("no term")
        val courses = coursesRepo.getCoursesFromNet(term)
        coursesRepo.saveCourses(courses)
    }

    fun getCourses() {
        getCoursesStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getCoursesWithoutState()
                getCoursesStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                getCoursesStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    fun setTimeTable(timeTable: String) {
        setTimeTableStateLiveData.value = SimpleState.Loading
        viewModelScope.launch {
            try {
                if(!TimeTableUtils.checkTimeTable(timeTable)) throw Exception("invalid time table")
                SettingDataStore.courseScheduleTimeTable.set(timeTable)
                setTimeTableStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                setTimeTableStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }
}