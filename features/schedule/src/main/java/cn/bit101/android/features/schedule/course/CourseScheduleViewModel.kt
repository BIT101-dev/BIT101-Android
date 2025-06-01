package cn.bit101.android.features.schedule.course

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.max

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


    // 课表相关信息
    val currentTermFlow = courseScheduleSettings.term.flow
    val timeTableStringFlow = courseScheduleSettings.timeTable.flow

    private val coursesFlow = coursesRepo.getCoursesFromLocal()

    private val examsFlow = coursesRepo.getExamsFromLocal()


    // 显示相关配置
    val showSaturdayFlow = courseScheduleSettings.showSaturday.flow
    val showSundayFlow = courseScheduleSettings.showSunday.flow
    val showBorderFlow = courseScheduleSettings.showBorder.flow
    val showHighlightTodayFlow = courseScheduleSettings.highlightToday.flow
    val showDividerFlow = courseScheduleSettings.showDivider.flow
    val showCurrentTimeFlow = courseScheduleSettings.showCurrentTime.flow
    val showExamInfoFlow = courseScheduleSettings.showExamInfo.flow

    val refreshCoursesStateLiveData = MutableLiveData<SimpleState?>(null)
    val forceRefreshCoursesStateLiveData = MutableLiveData<SimpleState?>(null)

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

    init {
        // 学期开始日期改变
        withScope {
            firstDayFlow.collect { firstDay ->
                val week = firstDay?.until(LocalDate.now(), ChronoUnit.WEEKS)?.plus(1)?.toInt() ?: Int.MAX_VALUE
                _weekFlow.value = maxOf(week, 1)
            }
        }

        // 课表、考试安排、周数、学期开始日期、时间表改变
        withScope {
            combine(
                coursesFlow,
                examsFlow,
                weekFlow,
                firstDayFlow,
                timeTableStringFlow
            ) { courses, exams, week, firstDay, timeTable ->
                val weekFirstDate = firstDay?.plusWeeks((week - 1).toLong()) ?: LocalDate.MAX
                val weekExams = exams.filter {
                    (weekFirstDate <= it.date) && (it.date < weekFirstDate.plusWeeks(1))
                }

                // 遍历一天的考试安排
                // 考试安排里记录的是精确的时间, 所以这里要额外做一步转换
                val weekExamSchedules = weekExams.map { exam ->
                    val beginSection = timeTable.withIndex().find {
                        exam.beginTime <= it.value.endTime
                    } ?: timeTable.withIndex().last()
                    val endSection = timeTable.withIndex().find {
                        exam.endTime <= it.value.endTime
                    } ?: timeTable.withIndex().last()

                    // 极端情况下, 考试的开始 / 结束时间可能对应时间表的课间, 此时应当让边界保持在中间线上, 也就是下一节课的开始位置
                    val beginSectionIndex = beginSection.index +
                            max(
                                0.0f,
                                exam.beginTime.until(beginSection.value.startTime, ChronoUnit.SECONDS).toFloat() /
                                        beginSection.value.endTime.until(
                                            beginSection.value.startTime,
                                            ChronoUnit.SECONDS
                                        )
                            )

                    val endSectionIndex = endSection.index +
                            max(
                                0.0f,
                                exam.endTime.until(endSection.value.startTime, ChronoUnit.SECONDS).toFloat() /
                                        endSection.value.endTime.until(
                                            endSection.value.startTime,
                                            ChronoUnit.SECONDS
                                        )
                            )

                    ScheduleItem(
                        dayOfWeek = exam.date.dayOfWeek.value,
                        startSection = beginSectionIndex,
                        endSection = endSectionIndex,
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

                _schedules.value = convertWeekSchedules(weekExamSchedules + weekCourseSchedules)
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


    // 将日程按照周一到周日分组排列
    private fun convertWeekSchedules(courses: List<ScheduleItem>): List<List<ScheduleItem>> {
        val finalSchedules = courses.sortedBy { it.startSection }
        val weekSchedules = mutableListOf<List<ScheduleItem>>()
        for (i in 1..7) {
            val daySchedules = mutableListOf<ScheduleItem>()
            for (schedule in finalSchedules) {
                if (schedule.dayOfWeek == i) {
                    daySchedules.add(schedule)
                }
            }
            weekSchedules.add(daySchedules)
        }
        return weekSchedules
    }
}