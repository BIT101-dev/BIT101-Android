package cn.bit101.android.data.repo

import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.android.data.repo.base.ManageRepo
import cn.bit101.android.data.repo.base.MessageRepo
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.data.repo.base.ReactionRepo
import cn.bit101.android.data.repo.base.UploadRepo
import cn.bit101.android.data.repo.base.UserRepo
import cn.bit101.android.data.repo.base.VersionRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds
    @Singleton
    abstract fun bindCalendarRepo(
        calendarRepo: DefaultDDLScheduleRepo
    ): DDLScheduleRepo

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
}