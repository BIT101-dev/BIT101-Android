package cn.bit101.android.config.setting

import cn.bit101.android.config.common.Transformer
import cn.bit101.android.config.common.map
import cn.bit101.android.config.common.toSettingItem
import cn.bit101.android.config.datastore.SettingDataStore
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.TimeTable
import cn.bit101.android.config.setting.base.toTimeTable
import cn.bit101.android.config.setting.base.toTimeTableString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

internal class DefaultCourseScheduleSettings @Inject constructor(
    settingDataStore: SettingDataStore
) : CourseScheduleSettings {

    /**
     * 课表第一天的转换器，用于将字符串转换为日期，或者将日期转换为字符串
     */
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

    /**
     * 课表的转换器，用于将字符串转换为课表，或者将课表转换为字符串
     */
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