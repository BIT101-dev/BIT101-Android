package cn.bit101.android.datastore

import android.util.Log
import androidx.datastore.preferences.core.*
import cn.bit101.android.datastore.base.DataStoreArgItem
import cn.bit101.android.datastore.base.DataStoreItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * @author flwfdd
 * @date 2023/4/4 19:53
 * @description _(:з」∠)_
 */

object SettingDataStore {
    // 设置模块
    // 是否开启旋转
    private val SETTING_ROTATE = booleanPreferencesKey("setting_rotate")
    val settingRotate = DataStoreItem(SETTING_ROTATE, false)


    // 是否自动适配系统主题
    private val SETTING_DYNAMIC_THEME = booleanPreferencesKey("setting_dynamic_theme")
    val settingDynamicTheme = DataStoreItem(SETTING_DYNAMIC_THEME, true)


    // 忽略更新版本
    private val SETTING_IGNORE_VERSION = longPreferencesKey("setting_ignore_version")
    val settingIgnoreVersion = DataStoreItem(SETTING_IGNORE_VERSION, -1L)

    // 是否禁用暗黑主题
    private val SETTING_DISABLE_DARK_THEME = booleanPreferencesKey("setting_disable_dark_theme")
    val settingDisableDarkTheme = DataStoreItem(SETTING_DISABLE_DARK_THEME, false)

    // 是否启用话廊
    private val SETTING_ENABLE_GALLERY = booleanPreferencesKey("setting_enable_gallery")
    val settingEnableGallery = DataStoreItem(SETTING_ENABLE_GALLERY, true)

    // 是否使用webVpn
    private val SETTING_USE_WEB_VPN = booleanPreferencesKey("setting_use_web_vpn")
    val settingUseWebVpn = DataStoreItem(SETTING_USE_WEB_VPN, false)

    // 课程表模块配置
    // 学期
    private val COURSE_SCHEDULE_TERM = stringPreferencesKey("course_schedule_term")
    val courseScheduleTerm = DataStoreItem(COURSE_SCHEDULE_TERM, "")

    // 学期开始日期
    private const val COURSE_SCHEDULE_FIRST_DAY_KEY_STRING = "course_schedule_first_day"
    private val courseScheduleFirstDayPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val courseScheduleFirstDay = DataStoreArgItem(
        COURSE_SCHEDULE_FIRST_DAY_KEY_STRING,
        { it?.format(courseScheduleFirstDayPattern) ?: "" },
        {
            try {
                LocalDate.parse(it, courseScheduleFirstDayPattern)
            } catch (e: Exception) {
                Log.e("SchoolSchedule", "Parse First Day Error $e")
                null
            }
        }
    )

    // 是否显示周六
    private val COURSE_SCHEDULE_SHOW_SATURDAY = booleanPreferencesKey("course_schedule_show_saturday")
    val courseScheduleShowSaturday = DataStoreItem(COURSE_SCHEDULE_SHOW_SATURDAY, true)

    // 是否显示周日
    private val COURSE_SCHEDULE_SHOW_SUNDAY = booleanPreferencesKey("course_schedule_show_sunday")
    val courseScheduleShowSunday = DataStoreItem(COURSE_SCHEDULE_SHOW_SUNDAY, true)

    // 是否显示边框
    private val COURSE_SCHEDULE_SHOW_BORDER = booleanPreferencesKey("course_schedule_show_border")
    val courseScheduleShowBorder = DataStoreItem(COURSE_SCHEDULE_SHOW_BORDER, false)

    // 是否高亮今日
    private val COURSE_SCHEDULE_SHOW_HIGHLIGHT_TODAY = booleanPreferencesKey("course_schedule_show_highlight_today")
    val courseScheduleShowHighlightToday = DataStoreItem(COURSE_SCHEDULE_SHOW_HIGHLIGHT_TODAY, true)

    // 是否显示节次分割线
    private val COURSE_SCHEDULE_SHOW_DIVIDER = booleanPreferencesKey("course_schedule_show_divider")
    val courseScheduleShowDivider = DataStoreItem(COURSE_SCHEDULE_SHOW_DIVIDER, true)

    // 是否显示当前时间
    private val COURSE_SCHEDULE_SHOW_CURRENT_TIME = booleanPreferencesKey("course_schedule_show_current_time")
    val courseScheduleShowCurrentTime = DataStoreItem(COURSE_SCHEDULE_SHOW_CURRENT_TIME, true)

    // 时间表
    private val COURSE_SCHEDULE_TIME_TABLE = stringPreferencesKey("course_schedule_time_table")
    val courseScheduleTimeTable = DataStoreItem(
        COURSE_SCHEDULE_TIME_TABLE,
        "08:00,08:45\n" +
                "08:50,09:35\n" +
                "09:55,10:40\n" +
                "10:45,11:30\n" +
                "11:35,12:20\n" +
                "13:20,14:05\n" +
                "14:10,14:55\n" +
                "15:15,16:00\n" +
                "16:05,16:50\n" +
                "16:55,17:40\n" +
                "18:30,19:15\n" +
                "19:20,20:05\n" +
                "20:10,20:55"

    )

    // 地图缩放倍率
    private val MAP_SCALE = floatPreferencesKey("map_scale")
    val mapScale = DataStoreItem(MAP_SCALE, 2f)

    // 乐学日程订阅链接
    private val LEXUE_CALENDAR_URL = stringPreferencesKey("lexue_calendar_url")
    val lexueCalendarUrl = DataStoreItem(LEXUE_CALENDAR_URL, "")

    // 日程临近改变颜色天数
    private val DDL_SCHEDULE_BEFORE_DAY = longPreferencesKey("ddl_schedule_before_day")
    val ddlScheduleBeforeDay = DataStoreItem(DDL_SCHEDULE_BEFORE_DAY, 7)

    // 日程过期继续显示天数
    private val DDL_SCHEDULE_AFTER_DAY = longPreferencesKey("ddl_schedule_after_day")
    val ddlScheduleAfterDay = DataStoreItem(DDL_SCHEDULE_AFTER_DAY, 3)


    // 主页设置
    private val HOME_PAGE = stringPreferencesKey("home_page")
    val settingHomePage = DataStoreItem(HOME_PAGE, "schedule")
}

