package cn.bit101.android.config.setting

import cn.bit101.android.config.setting.base.*
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

    @Binds
    @Singleton
    abstract fun bindGallerySettings(
        gallerySettings: DefaultGallerySettings
    ): GallerySettings
}