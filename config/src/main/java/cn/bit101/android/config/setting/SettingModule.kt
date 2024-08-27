package cn.bit101.android.config.setting

import cn.bit101.android.config.setting.base.AboutSettings
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.config.setting.base.MapSettings
import cn.bit101.android.config.setting.base.PageSettings
import cn.bit101.android.config.setting.base.ThemeSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal abstract class SettingModule {
    @Binds
    @Singleton
    abstract fun bindPageSettings(
        pageSettings: DefaultPageSettings
    ): PageSettings

    @Binds
    @Singleton
    abstract fun bindCourseScheduleSettings(
        courseScheduleSettings: DefaultCourseScheduleSettings
    ): CourseScheduleSettings

    @Binds
    @Singleton
    abstract fun bindDDLScheduleSettings(
        ddlSettings: DefaultDDLSettings
    ): DDLSettings

    @Binds
    @Singleton
    abstract fun bindThemeSettings(
        themeSettings: DefaultThemeSettings
    ): ThemeSettings

    @Binds
    @Singleton
    abstract fun bindAboutSettings(
        aboutSettings: DefaultAboutSettings
    ): AboutSettings

    @Binds
    @Singleton
    abstract fun bindMapSettings(
        mapSettings: DefaultMapSettings
    ): MapSettings
}