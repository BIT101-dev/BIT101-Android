package cn.bit101.android.features.setting.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.toTimeTable
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import cn.bit101.android.data.repo.CourseSync
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.withSimpleErrorDataState
import cn.bit101.android.features.common.helper.successMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleDataStateLiveData
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import cn.bit101.android.features.common.utils.ScheduleCreateInfo
import cn.bit101.android.features.common.utils.addScheduleToSystemCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal data class SettingData(
    val showDivider: Boolean,
    val showSaturday: Boolean,
    val showSunday: Boolean,
    val showHighlightToday: Boolean,
    val showBorder: Boolean,
    val showCurrentTime: Boolean,
    val showExamInfo: Boolean,
) {
    companion object {
        val default = SettingData(
            showDivider = false,
            showSaturday = false,
            showSunday = false,
            showHighlightToday = false,
            showBorder = false,
            showCurrentTime = false,
            showExamInfo = false,
        )
    }
}

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    private val coursesRepo: CoursesRepo,
    private val loginRepo: SchoolLoginRepo,
    private val courseSync: CourseSync,
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
        courseScheduleSettings.showCurrentTime.flow,
        courseScheduleSettings.showExamInfo.flow,
    ) { settings ->
        SettingData(
            showSaturday = settings[0],
            showSunday = settings[1],
            showBorder = settings[2],
            showHighlightToday = settings[3],
            showDivider = settings[4],
            showCurrentTime = settings[5],
            showExamInfo = settings[6],
        )
    }

    val timeTableFlow = courseScheduleSettings.timeTable.flow

    // 学期列表获取状态
    val getTermListStateLiveData = MutableLiveData<SimpleDataState<List<String>>?>(null)

    private val _syncState = MutableStateFlow<SimpleErrorDataState<String>?>(null)
    val syncState = _syncState.asStateFlow()

    // 设置时间表的状态
    val setTimeTableStateLiveData = MutableLiveData<SimpleState?>(null)

    // 自定义日程获取状态
    val getCustomScheduleStateLiveData = MutableLiveData<SimpleDataState<List<CustomScheduleEntity>>?>(null)
    // 自定义日程删除状态
    val deleteCustomScheduleStateLiveData = MutableLiveData<SimpleState?>(null)
    // 自定义日程编辑状态
    val editCustomScheduleStateLiveData = MutableLiveData<SimpleState?>(null)

    val addScheduleToSysCalendarStateLiveData = MutableLiveData<SimpleState?>(null)

    fun getTermList() = withSimpleDataStateLiveData(getTermListStateLiveData) {
        loginRepo.withSchoolSession(coursesRepo::getTermListFromNet)
    }

    fun setCurrentTerm(term: String) = withSimpleErrorDataState(_syncState) {
        courseSync.refresh(term).successMessage()
    }

    fun getCustomSchedules() = withSimpleDataStateLiveData(getCustomScheduleStateLiveData) {
        coursesRepo
            .getCustomSchedules()
            .first()
            .sortedBy { it.date.atTime(it.beginTime) }
    }

    fun deleteCustomSchedule(scheduleEntity: CustomScheduleEntity) = withSimpleStateLiveData(deleteCustomScheduleStateLiveData) {
        coursesRepo.deleteCustomSchedule(scheduleEntity)
    }

    fun addScheduleToSysCalendar(context: Context, schedule: CustomScheduleEntity) = withSimpleStateLiveData(addScheduleToSysCalendarStateLiveData) {
        addScheduleToSystemCalendar(context, schedule)
    }

    fun updateCustomSchedule(
        scheduleEntity: CustomScheduleEntity,
        scheduleCreateInfo: ScheduleCreateInfo
    ) = withSimpleStateLiveData(editCustomScheduleStateLiveData) {
        coursesRepo.updateCustomSchedule(
            scheduleCreateInfo.toEntity().copy(
                id = scheduleEntity.id,
            )
        )
    }

    fun setSettingData(settingData: SettingData) = withScope {
        courseScheduleSettings.showDivider.set(settingData.showDivider)
        courseScheduleSettings.showSaturday.set(settingData.showSaturday)
        courseScheduleSettings.showSunday.set(settingData.showSunday)
        courseScheduleSettings.highlightToday.set(settingData.showHighlightToday)
        courseScheduleSettings.showBorder.set(settingData.showBorder)
        courseScheduleSettings.showCurrentTime.set(settingData.showCurrentTime)
        courseScheduleSettings.showExamInfo.set(settingData.showExamInfo)
    }

    fun getFirstDay() = withSimpleErrorDataState(_syncState) {
        courseSync.refreshFirstDay()
        "学期开始日期已更新"
    }

    fun getCourses() = withSimpleErrorDataState(_syncState) {
        courseSync.refreshCourses()
        "课程表已更新"
    }

    fun getExams() = withSimpleErrorDataState(_syncState) {
        val count = courseSync.refreshExams()
        if (count > 0) "更新了 $count 条考试安排" else "考试安排已是最新"
    }

    fun setTimeTable(timeTableStr: String) = withSimpleStateLiveData(setTimeTableStateLiveData) {
        courseScheduleSettings.timeTable.set(timeTableStr.toTimeTable())
    }
}