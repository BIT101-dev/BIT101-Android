package cn.bit101.android.data.repo

import cn.bit101.android.data.repo.base.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepoModule {
    @Binds
    @Singleton
    abstract fun bindCalendarRepo(
        calendarRepo: DefaultDDLScheduleRepo
    ): DDLScheduleRepo

    /** 课程中心（eclass）—— 学校 2026 年起替代乐学的作业来源。 */
    @Binds
    @Singleton
    abstract fun bindEclassRepo(
        eclassRepo: DefaultEclassRepo
    ): EclassRepo

    @Binds
    @Singleton
    abstract fun bindVersionRepo(
        versionRepo: DefaultVersionRepo
    ): VersionRepo

    @Binds
    @Singleton
    abstract fun bindScheduleRepo(
        scheduleRepo: DefaultCoursesRepo
    ): CoursesRepo

    @Binds
    @Singleton
    abstract fun bindPosterRepo(
        posterRepo: DefaultPosterRepo
    ): PosterRepo

    @Binds
    @Singleton
    abstract fun bindReactionRepo(
        reactionRepo: DefaultReactionRepo
    ): ReactionRepo

    @Binds
    @Singleton
    abstract fun bindUploadRepo(
        uploadRepo: DefaultUploadRepo
    ): UploadRepo

    @Binds
    @Singleton
    abstract fun bindManageRepo(
        manageRepo: DefaultManageRepo
    ): ManageRepo

    @Binds
    @Singleton
    abstract fun bindUserRepo(
        userRepo: DefaultUserRepo
    ): UserRepo

    @Binds
    @Singleton
    abstract fun bindMessageRepo(
        messageRepo: DefaultMessageRepo
    ): MessageRepo

    @Binds
    @Singleton
    abstract fun bindLoginRepo(
        loginRepo: DefaultLoginRepo
    ): LoginRepo

    @Binds
    @Singleton
    abstract fun bindFreeClassroomRepo(
        freeClassroomRepo: DefaultFreeClassroomRepo
    ): FreeClassroomRepo
}