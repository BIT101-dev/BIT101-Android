package cn.bit101.android.data.net

import cn.bit101.android.data.net.base.APIManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetModule {
    @Binds
    @Singleton
    abstract fun bindAPIManager(
        apiManager: DefaultAPIManager
    ): APIManager
}