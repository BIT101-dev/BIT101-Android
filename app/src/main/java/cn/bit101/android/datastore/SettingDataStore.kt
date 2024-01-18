package cn.bit101.android.datastore

import androidx.datastore.preferences.core.*
import cn.bit101.android.datastore.basic.PreferencesDataStoreItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author flwfdd
 * @date 2023/4/4 19:53
 * @description _(:з」∠)_
 */
@Singleton
class SettingDataStore @Inject constructor() {
    // 设置模块
    // 是否开启旋转
    private val SETTING_ROTATE = booleanPreferencesKey("setting_rotate")
    val settingRotate = PreferencesDataStoreItem(SETTING_ROTATE, false)

    // 是否自动适配系统主题
    private val SETTING_DYNAMIC_THEME = booleanPreferencesKey("setting_dynamic_theme")
    val settingDynamicTheme = PreferencesDataStoreItem(SETTING_DYNAMIC_THEME, true)

    // 忽略更新版本
    private val SETTING_IGNORE_VERSION = longPreferencesKey("setting_ignore_version")
    val settingIgnoreVersion = PreferencesDataStoreItem(SETTING_IGNORE_VERSION, -1L)

    // 是否禁用暗黑主题
    private val SETTING_DARK_THEME = stringPreferencesKey("setting_dark_theme")
    val settingDarkTheme = PreferencesDataStoreItem(SETTING_DARK_THEME, "system")

    // 是否使用webVpn
    private val SETTING_USE_WEB_VPN = booleanPreferencesKey("setting_use_web_vpn")
    val settingUseWebVpn = PreferencesDataStoreItem(SETTING_USE_WEB_VPN, false)

    // 课程表模块配置
    // 学期
    private val COURSE_SCHEDULE_TERM = stringPreferencesKey("course_schedule_term")
    val courseScheduleTerm = PreferencesDataStoreItem(COURSE_SCHEDULE_TERM, "")

    // 学期开始日期
    private val COURSE_SCHEDULE_FIRST_DAY = stringPreferencesKey("course_schedule_first_day")
    val courseScheduleFirstDay = PreferencesDataStoreItem(COURSE_SCHEDULE_FIRST_DAY, "")

    // 是否显示周六
    private val COURSE_SCHEDULE_SHOW_SATURDAY = booleanPreferencesKey("course_schedule_show_saturday")
    val courseScheduleShowSaturday = PreferencesDataStoreItem(COURSE_SCHEDULE_SHOW_SATURDAY, true)

    // 是否显示周日
    private val COURSE_SCHEDULE_SHOW_SUNDAY = booleanPreferencesKey("course_schedule_show_sunday")
    val courseScheduleShowSunday = PreferencesDataStoreItem(COURSE_SCHEDULE_SHOW_SUNDAY, true)

    // 是否显示边框
    private val COURSE_SCHEDULE_SHOW_BORDER = booleanPreferencesKey("course_schedule_show_border")
    val courseScheduleShowBorder = PreferencesDataStoreItem(COURSE_SCHEDULE_SHOW_BORDER, false)

    // 是否高亮今日
    private val COURSE_SCHEDULE_SHOW_HIGHLIGHT_TODAY = booleanPreferencesKey("course_schedule_show_highlight_today")
    val courseScheduleShowHighlightToday = PreferencesDataStoreItem(COURSE_SCHEDULE_SHOW_HIGHLIGHT_TODAY, true)

    // 是否显示节次分割线
    private val COURSE_SCHEDULE_SHOW_DIVIDER = booleanPreferencesKey("course_schedule_show_divider")
    val courseScheduleShowDivider = PreferencesDataStoreItem(COURSE_SCHEDULE_SHOW_DIVIDER, true)

    // 是否显示当前时间
    private val COURSE_SCHEDULE_SHOW_CURRENT_TIME = booleanPreferencesKey("course_schedule_show_current_time")
    val courseScheduleShowCurrentTime = PreferencesDataStoreItem(COURSE_SCHEDULE_SHOW_CURRENT_TIME, true)

    // 时间表
    private val COURSE_SCHEDULE_TIME_TABLE = stringPreferencesKey("course_schedule_time_table")
    val courseScheduleTimeTable = PreferencesDataStoreItem(
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
    val mapScale = PreferencesDataStoreItem(MAP_SCALE, 2f)

    // 乐学日程订阅链接
    private val LEXUE_CALENDAR_URL = stringPreferencesKey("lexue_calendar_url")
    val lexueCalendarUrl = PreferencesDataStoreItem(LEXUE_CALENDAR_URL, "")

    // 日程临近改变颜色天数
    private val DDL_SCHEDULE_BEFORE_DAY = longPreferencesKey("ddl_schedule_before_day")
    val ddlScheduleBeforeDay = PreferencesDataStoreItem(DDL_SCHEDULE_BEFORE_DAY, 7)

    // 日程过期继续显示天数
    private val DDL_SCHEDULE_AFTER_DAY = longPreferencesKey("ddl_schedule_after_day")
    val ddlScheduleAfterDay = PreferencesDataStoreItem(DDL_SCHEDULE_AFTER_DAY, 3)


    // 主页设置
    private val HOME_PAGE = stringPreferencesKey("home_page")
    val settingHomePage = PreferencesDataStoreItem(HOME_PAGE, "schedule")

    // 页面的顺序
    private val SETTING_PAGE_ORDER = stringPreferencesKey("setting_page_order")
    val settingPageOrder = PreferencesDataStoreItem(SETTING_PAGE_ORDER, "schedule,map,bit101-web,gallery,mine")

    // 页面是否可见，不可见的在字符串中
    private val SETTING_PAGE_VISIBLE = stringPreferencesKey("setting_page_visible")
    val settingPageVisible = PreferencesDataStoreItem(SETTING_PAGE_VISIBLE, "")

    private val SETTING_AUTO_DETECT_UPGRADE = booleanPreferencesKey("setting_auto_detect_upgrade")
    val settingAutoDetectUpgrade = PreferencesDataStoreItem(SETTING_AUTO_DETECT_UPGRADE, true)

    // 上一次的版本，用来记录是不是第一次启动
    private val SETTING_LAST_VERSION = longPreferencesKey("setting_last_version")
    val settingLastVersion = PreferencesDataStoreItem(SETTING_LAST_VERSION, -1)
}

