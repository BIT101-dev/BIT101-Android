package cn.bit101.android.repo

import cn.bit101.android.repo.base.DDLScheduleRepo
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.repo.base.ManageRepo
import cn.bit101.android.repo.base.MessageRepo
import cn.bit101.android.repo.base.ReactionRepo
import cn.bit101.android.repo.base.UploadRepo
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.android.repo.base.VersionRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds
    abstract fun bindCalendarRepo(calendarRepo: DefaultDDLScheduleRepo): DDLScheduleRepo
    @Binds
    abstract fun bindVersionRepo(versionRepo: DefaultVersionRepo): VersionRepo
    @Binds
    abstract fun bindScheduleRepo(scheduleRepo: DefaultCoursesRepo): CoursesRepo
    @Binds
    abstract fun bindPosterRepo(posterRepo: DefaultPosterRepo): PosterRepo
    @Binds
    abstract fun bindReactionRepo(reactionRepo: DefaultReactionRepo): ReactionRepo
    @Binds
    abstract fun bindUploadRepo(uploadRepo: DefaultUploadRepo): UploadRepo
    @Binds
    abstract fun bindManageRepo(manageRepo: DefaultManageRepo): ManageRepo
    @Binds
    abstract fun bindUserRepo(userRepo: DefaultUserRepo): UserRepo
    @Binds
    abstract fun bindMessageRepo(messageRepo: DefaultMessageRepo): MessageRepo
}