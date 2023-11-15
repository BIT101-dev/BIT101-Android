package cn.bit101.android.status

import cn.bit101.android.repo.DefaultDDLScheduleRepo
import cn.bit101.android.repo.base.DDLScheduleRepo
import cn.bit101.android.status.base.LoginStatusManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StatusManagerModule {
    @Binds
    abstract fun bindLoginStatusManager(
        loginStatusManager: DefaultLoginStatusManager
    ): LoginStatusManager
}