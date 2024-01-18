package cn.bit101.android.manager

import cn.bit101.android.manager.base.AboutSettingManager
import cn.bit101.android.manager.base.CourseScheduleSettingManager
import cn.bit101.android.manager.base.DDLSettingManager
import cn.bit101.android.manager.base.LoginStatusManager
import cn.bit101.android.manager.base.MapSettingManager
import cn.bit101.android.manager.base.PageSettingManager
import cn.bit101.android.manager.base.ThemeSettingManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Binds
    abstract fun bindLoginStatusManager(
        loginStatusManager: DefaultLoginStatusManager
    ): LoginStatusManager

    @Binds
    abstract fun bindPageSettingManager(
        pageSettingManager: DefaultPageSettingManager
    ): PageSettingManager

    @Binds
    abstract fun bindCourseScheduleSettingManager(
        courseScheduleSettingManager: DefaultCourseScheduleSettingManager
    ): CourseScheduleSettingManager

    @Binds
    abstract fun bindDDLScheduleSettingManager(
        ddlSettingManager: DefaultDDLSettingManager
    ): DDLSettingManager

    @Binds
    abstract fun bindThemeSettingManager(
        themeSettingManager: DefaultThemeSettingManager
    ): ThemeSettingManager

    @Binds
    abstract fun bindAboutSettingManager(
        aboutSettingManager: DefaultAboutSettingManager
    ): AboutSettingManager

    @Binds
    abstract fun bindMapSettingManager(
        mapSettingManager: DefaultMapSettingManager
    ): MapSettingManager

}