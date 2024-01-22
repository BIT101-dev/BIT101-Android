package cn.bit101.android.config.user

import cn.bit101.android.config.user.base.LoginStatus
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
    abstract fun bindLoginStatus(
        loginStatus: DefaultLoginStatus
    ): LoginStatus
}