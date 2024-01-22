package cn.bit101.android.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


private const val FILE_NAME = "bit101.db"


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BIT101Database {
        return Room.databaseBuilder(
            context,
            BIT101Database::class.java,
            FILE_NAME
        ).build()
    }
}