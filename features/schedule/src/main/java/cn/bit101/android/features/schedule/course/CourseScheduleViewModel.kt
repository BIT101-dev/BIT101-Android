package cn.bit101.android.features.schedule.course

import android.content.Context
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.TimeTable
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import cn.bit101.android.features.common.utils.ScheduleCreateInfo
import cn.bit101.android.features.common.utils.addScheduleToSystemCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * @author flwfdd
 * @date 2023/3/31 23:30
 * @description _(:з」∠)_
 */

internal data class SettingData(
    val showSaturday: Boolean,
    val showSunday: Boolean,
    val showDivider: Boolean,
    val showHighlightToday: Boolean,
    val showBorder: Boolean,
    val showCurrentTime: Boolean,
    val showExamInfo: Boolean,
)

@HiltViewModel
internal class CourseScheduleViewModel @Inject constructor(
    private val coursesRepo: CoursesRepo,
    private val courseScheduleSettings: CourseScheduleSettings
) : ViewModel() {
    private val _schedules = MutableStateFlow<List<List<ScheduleItem>>>(emptyList())
    val schedules: StateFlow<List<List<ScheduleItem>>> = _schedules.asStateFlow()

    val firstDayFlow = courseScheduleSettings.firstDay.flow

    private val _weekFlow = MutableStateFlow(Int.MAX_VALUE)
    val weekFlow: StateFlow<Int> = _weekFlow.asStateFlow()


    // 显示相关配置
    val showSaturdayFlow = courseScheduleSettings.showSaturday.flow
    val showSundayFlow = courseScheduleSettings.showSunday.flow
    val showBorderFlow = courseScheduleSettings.showBorder.flow
    val showHighlightTodayFlow = courseScheduleSettings.highlightToday.flow
    val showDividerFlow = courseScheduleSettings.showDivider.flow
    val showCurrentTimeFlow = courseScheduleSettings.showCurrentTime.flow
    val showExamInfoFlow = courseScheduleSettings.showExamInfo.flow


    // 课表相关信息
    val currentTermFlow = courseScheduleSettings.term.flow
    val timeTableStringFlow = courseScheduleSettings.timeTable.flow

    private val coursesFlow = coursesRepo.getCoursesFromLocal()

    private val examsFlow = combine(
        coursesRepo.getExamsFromLocal(),
        showExamInfoFlow
    ) { exams, showExamInfo ->
        if(showExamInfo)
            exams
        else
            emptyList()
    }

    private val customSchedulesFlow = coursesRepo.getCustomSchedules()


    val refreshCoursesStateLiveData = MutableLiveData<SimpleState?>(null)
    val forceRefreshCoursesStateLiveData = MutableLiveData<SimpleState?>(null)

    val addEditCustomScheduleStateLiveData = MutableLiveData<SimpleState?>(null)
    val deleteCustomScheduleStateLiveData = MutableLiveData<SimpleState?>(null)

    val addScheduleToSysCalendarStateLiveData = MutableLiveData<SimpleState?>(null)

    // 课程详情数据，如果为 null 就不显示该对话框
    val _showCourseDetail = MutableStateFlow<CourseScheduleEntity?>(null)
    val showCourseDetail = _showCourseDetail.asStateFlow()

    fun clearShowCourseDetail() {
        _showCourseDetail.value = null
    }

    // 考试详情数据, 如果为 null 就不显示该对话框
    val _showExamDetail = MutableStateFlow<ExamScheduleEntity?>(null)
    val showExamDetail = _showExamDetail.asStateFlow()

    fun clearShowExamDetail() {
        _showExamDetail.value = null
    }

    // 自定义日程数据, 如果为 null 就不显示该对话框
    val _showCustomScheduleDetail = MutableStateFlow<CustomScheduleEntity?>(null)
    val showCustomScheduleDetail = _showCustomScheduleDetail.asStateFlow()

    fun clearCustomScheduleDetail() {
        _showCustomScheduleDetail.value = null
    }
    fun showCustomScheduleDetail(scheduleEntity: CustomScheduleEntity) {
        _showCustomScheduleDetail.value = scheduleEntity
    }
    fun deleteCustomSchedule(scheduleEntity: CustomScheduleEntity) = withSimpleStateLiveData(deleteCustomScheduleStateLiveData) {
        coursesRepo.deleteCustomSchedule(scheduleEntity)
    }

    // 把日程添加到系统日历中
    fun addScheduleToSysCalendar(context: Context, schedule: CustomScheduleEntity) = withSimpleStateLiveData(addScheduleToSysCalendarStateLiveData) {
        addScheduleToSystemCalendar(context, schedule)
    }
    fun addScheduleToSysCalendar(context: Context, schedule: ExamScheduleEntity) = withSimpleStateLiveData(addScheduleToSysCalendarStateLiveData) {
        addScheduleToSystemCalendar(context, schedule)
    }

    init {
        // 学期开始日期改变
        withScope {
            firstDayFlow.collect { firstDay ->
                val week = firstDay?.until(LocalDate.now(), ChronoUnit.WEEKS)?.plus(1)?.toInt() ?: Int.MAX_VALUE
                _weekFlow.value = maxOf(week, 1)
            }
        }

        // 课表、考试安排、周数、学期开始日期、时间表、自定义日程改变
        withScope {
            combine(
                // combine 至多比较方便地组合 5 个参数, 所以额外打包一次
                combine(
                    coursesFlow,
                    examsFlow,
                    customSchedulesFlow
                ) { courses, exams, schedules ->
                    Triple(courses, exams, schedules)
                },
                weekFlow,
                firstDayFlow,
                timeTableStringFlow,
            ) { schedules, week, firstDay, timeTable ->
                val courses = schedules.first
                val exams = schedules.second
                val customSchedules = schedules.third

                val weekFirstDate = firstDay?.plusWeeks((week - 1).toLong()) ?: LocalDate.MAX
                val weekExams = exams.filter {
                    (weekFirstDate <= it.date) && (it.date < weekFirstDate.plusWeeks(1))
                }

                val weekExamSchedules = weekExams.map { exam ->
                    ScheduleItem(
                        dayOfWeek = exam.date.dayOfWeek.value,
                        startSection = convertTimeToSection(
                            time = exam.beginTime,
                            timeTable = timeTable,
                        ),
                        endSection = convertTimeToSection(
                            time = exam.endTime,
                            timeTable = timeTable,
                        ),
                        title = "(考试)\n${exam.name}",
                        subtitle = exam.classroom,
                        onClick = { _showExamDetail.value = exam },
                        color = ScheduleColorEnum.Exam
                    )
                }

                val weekCourses = courses.filter { course ->
                    course.weeks.contains("[$week]")
                }

                val weekCourseSchedules = weekCourses.map { course ->
                    ScheduleItem(
                        dayOfWeek = course.weekday,
                        startSection = course.start_section.toFloat() - 1,
                        endSection = course.end_section.toFloat(),
                        title = course.name,
                        subtitle = course.classroom,
                        onClick = { _showCourseDetail.value = course },
                        color = ScheduleColorEnum.Course
                    )
                }

                val weekCustomSchedules = customSchedules.filter {
                    (weekFirstDate <= it.date) && (it.date < weekFirstDate.plusWeeks(1))
                }.map { schedule ->
                    ScheduleItem(
                        dayOfWeek = schedule.date.dayOfWeek.value,
                        startSection = convertTimeToSection(
                            time = schedule.beginTime,
                            timeTable = timeTable,
                        ),
                        endSection = convertTimeToSection(
                            time = schedule.endTime,
                            timeTable = timeTable,
                        ),
                        title = schedule.title,
                        subtitle = schedule.subtitle,
                        onClick = { showCustomScheduleDetail(schedule) },
                        color = ScheduleColorEnum.Custom
                    )
                }

                _schedules.value = convertWeekSchedules(
                    weekExamSchedules + weekCourseSchedules + weekCustomSchedules
                )
            }.collect()
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
     * 强制刷新课表及考试安排，需要从网络中重新获取课表和考试安排
     */
    fun forceRefreshCourses() = withSimpleStateLiveData(refreshCoursesStateLiveData) {
        // 获得学期
        val term = coursesRepo.getCurrentTermFromNet()
        courseScheduleSettings.term.set(term)

        // 获取课表
        val courses = coursesRepo.getCoursesFromNet(term)
        coursesRepo.saveCourses(courses)

        // 获取考试安排
        val exams = coursesRepo.getExamsFromNet(term)
        coursesRepo.saveExams(exams)

        // 获取学期第一天
        val firstDay = coursesRepo.getFirstDayFromNet(term)
        courseScheduleSettings.firstDay.set(firstDay)
    }

    /**
     * 添加自定义日程
     */
    fun addCustomSchedule(scheduleCreateInfo: ScheduleCreateInfo) = withSimpleStateLiveData(addEditCustomScheduleStateLiveData) {
        coursesRepo.addCustomSchedule(scheduleCreateInfo.toEntity())
    }

    /**
     * 更新自定义日程
     */
    fun updateCustomSchedule(
        scheduleEntity: CustomScheduleEntity,
        scheduleCreateInfo: ScheduleCreateInfo
    ) = withSimpleStateLiveData(addEditCustomScheduleStateLiveData) {
        coursesRepo.updateCustomSchedule(
            scheduleCreateInfo.toEntity().copy(
                id = scheduleEntity.id,
            )
        )
    }


    // 将日程按照周一到周日分组排列
    // 同时修改日程开始和结束时间 (必要时删除日程) 以保证日程间不会相互重叠
    private fun convertWeekSchedules(courses: List<ScheduleItem>): List<List<ScheduleItem>> {
        val finalSchedules = courses.sortedBy { it.startSection }
        val weekSchedules = mutableListOf<List<ScheduleItem>>()
        for (i in 1..7) {
            val daySchedules = mutableListOf<ScheduleItem>()
            for (schedule in finalSchedules) {
                if (schedule.dayOfWeek == i) {
                    if (daySchedules.isNotEmpty()
                        && daySchedules.last().endSection > schedule.startSection
                    ) {
                        if (daySchedules.last().endSection < schedule.endSection) {
                            daySchedules.add(
                                schedule.copy(
                                    startSection = daySchedules.last().endSection,
                                )
                            )
                        }
                        // 否则说明日程完全被覆盖, 直接删掉 (不加进去)
                        // 但正常使用场景下这其实是不该发生的......
                    } else {
                        daySchedules.add(schedule)
                    }
                }
            }
            weekSchedules.add(daySchedules)
        }
        return weekSchedules
    }

    // 将精确时间对应到课程表上的节次, 返回浮点数
    // 0 对应一天开始, 1 对应第一节课刚好上完, 第二节课刚刚开始 (因为时间表数据不连续, 课间是不会体现出来的)
    private fun convertTimeToSection(time: LocalTime, timeTable: TimeTable): Float {
        val section = timeTable.withIndex().find {
            time <= it.value.endTime
        } ?: timeTable.withIndex().last()

        // 时间点可能过早或过晚或位于课间, 此时无法表达在课表上, 规约到最接近的位置
        return section.index +
                clamp(
                    time.until(section.value.startTime, ChronoUnit.SECONDS).toFloat() /
                            section.value.endTime.until(
                                section.value.startTime,
                                ChronoUnit.SECONDS
                            ),
                    0.0f,
                    1.0f
                )
    }
}