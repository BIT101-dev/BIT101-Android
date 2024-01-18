package cn.bit101.android.database

import androidx.room.Room
import cn.bit101.android.App
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


private const val FILE_NAME = "bit101.db"


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(): BIT101Database {
        return Room.databaseBuilder(
            App.context,
            BIT101Database::class.java,
            FILE_NAME
        ).build()
    }
}