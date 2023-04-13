package cn.bit101.android.database

import android.util.Log
import androidx.datastore.preferences.core.*
import cn.bit101.android.App
import cn.bit101.android.App.Companion.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * @author flwfdd
 * @date 2023/4/4 19:53
 * @description _(:з」∠)_
 */

const val TAG = "DataStore"

class DataStore {
    companion object {
        fun setString(key: Preferences.Key<String>, value: String) {
            CoroutineScope(Dispatchers.IO).launch {
                App.context.dataStore.edit { preferences ->
                    preferences[key] = value
                }
            }
        }

        fun setBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                App.context.dataStore.edit { preferences ->
                    preferences[key] = value
                }
            }
        }

        // 课程表模块配置

        // 学期
        val COURSE_SCHEDULE_TERM = stringPreferencesKey("course_schedule_term")
        val courseScheduleTermFlow: Flow<String?> = App.context.dataStore.data
            .map { preferences ->
                preferences[COURSE_SCHEDULE_TERM]
            }

        // 学期开始日期
        private val COURSE_SCHEDULE_FIRST_DAY = stringPreferencesKey("course_schedule_first_day")
        private val courseScheduleFirstDayPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fun setCourseScheduleFirstDay(value: LocalDate) {
            setString(COURSE_SCHEDULE_FIRST_DAY, value.format(courseScheduleFirstDayPattern))
        }

        val courseScheduleFirstDayFlow: Flow<LocalDate?> = App.context.dataStore.data
            .map { preferences ->
                if(preferences[COURSE_SCHEDULE_FIRST_DAY] == null) {
                    return@map null
                }
                try {
                    LocalDate.parse(
                        preferences[COURSE_SCHEDULE_FIRST_DAY] ?: "",
                        courseScheduleFirstDayPattern
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "courseScheduleFirstDayFlow", e)
                    null
                }
            }

        // 是否显示周六
        val COURSE_SCHEDULE_SHOW_SATURDAY =
            booleanPreferencesKey("course_schedule_show_saturday")
        val courseScheduleShowSaturdayFlow: Flow<Boolean> = App.context.dataStore.data
            .map { preferences ->
                preferences[COURSE_SCHEDULE_SHOW_SATURDAY] ?: true
            }

        // 是否显示周日
        val COURSE_SCHEDULE_SHOW_SUNDAY =
            booleanPreferencesKey("course_schedule_show_sunday")
        val courseScheduleShowSundayFlow: Flow<Boolean> = App.context.dataStore.data
            .map { preferences ->
                preferences[COURSE_SCHEDULE_SHOW_SUNDAY] ?: true
            }

        // 是否显示边框
        val COURSE_SCHEDULE_SHOW_BORDER =
            booleanPreferencesKey("course_schedule_show_border")
        val courseScheduleShowBorderFlow: Flow<Boolean> = App.context.dataStore.data
            .map { preferences ->
                preferences[COURSE_SCHEDULE_SHOW_BORDER] ?: false
            }

        // 是否高亮今日
        val COURSE_SCHEDULE_SHOW_HIGHLIGHT_TODAY =
            booleanPreferencesKey("course_schedule_show_highlight_today")
        val courseScheduleShowHighlightTodayFlow: Flow<Boolean> = App.context.dataStore.data
            .map { preferences ->
                preferences[COURSE_SCHEDULE_SHOW_HIGHLIGHT_TODAY] ?: true
            }

        // 是否显示节次分割线
        val COURSE_SCHEDULE_SHOW_DIVIDER =
            booleanPreferencesKey("course_schedule_show_divider")
        val courseScheduleShowDividerFlow: Flow<Boolean> = App.context.dataStore.data
            .map { preferences ->
                preferences[COURSE_SCHEDULE_SHOW_DIVIDER] ?: true
            }
    }
}

