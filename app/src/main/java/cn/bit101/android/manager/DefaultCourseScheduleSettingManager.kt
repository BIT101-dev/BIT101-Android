package cn.bit101.android.manager

import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.manager.base.CourseScheduleSettingManager
import cn.bit101.android.manager.base.TimeTable
import cn.bit101.android.manager.basic.SettingItem
import cn.bit101.android.manager.base.TimeTableItem
import cn.bit101.android.manager.base.toTimeTable
import cn.bit101.android.manager.base.toTimeTableString
import cn.bit101.android.manager.basic.Transformer
import cn.bit101.android.manager.basic.map
import cn.bit101.android.manager.basic.toSettingItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DefaultCourseScheduleSettingManager @Inject constructor(
    private val settingDataStore: SettingDataStore,
) : CourseScheduleSettingManager {

    private val firstDayTransformer = object : Transformer<String, LocalDate?> {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        override fun invokeTo(value: String): LocalDate? {
            return try {
                LocalDate.parse(value, formatter)
            } catch (_: Exception) {
                null
            }
        }

        override fun invokeFrom(value: LocalDate?): String {
            return value?.format(formatter) ?: ""
        }
    }

    private val timeTableTransformer = object : Transformer<String, TimeTable> {
        override fun invokeTo(value: String): TimeTable {
            return value.toTimeTable()
        }

        override fun invokeFrom(value: TimeTable): String {
            return value.toTimeTableString()
        }
    }

    override val term = settingDataStore.courseScheduleTerm.toSettingItem()

    override val firstDay = settingDataStore.courseScheduleFirstDay.toSettingItem().map(firstDayTransformer)

    override val highlightToday = settingDataStore.courseScheduleShowHighlightToday.toSettingItem()

    override val showSaturday = settingDataStore.courseScheduleShowSaturday.toSettingItem()

    override val showSunday = settingDataStore.courseScheduleShowSunday.toSettingItem()

    override val showBorder = settingDataStore.courseScheduleShowBorder.toSettingItem()

    override val showDivider = settingDataStore.courseScheduleShowDivider.toSettingItem()

    override val showCurrentTime = settingDataStore.courseScheduleShowCurrentTime.toSettingItem()

    override val timeTable = settingDataStore.courseScheduleTimeTable.toSettingItem().map(timeTableTransformer)
}