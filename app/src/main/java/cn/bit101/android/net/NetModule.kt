package cn.bit101.android.net

import cn.bit101.android.net.base.APIManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetModule {
    @Binds
    @Singleton
    abstract fun bindAPIManager(
        apiManager: DefaultAPIManager
    ): APIManager
}