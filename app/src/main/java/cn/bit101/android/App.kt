package cn.bit101.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import cn.bit101.android.database.BIT101Database

/**
 * @author flwfdd
 * @date 2023/3/16 23:19
 * @description _(:з」∠)_
 */
class App: Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        val DB: BIT101Database by lazy {
            Room.databaseBuilder(
                context,
                BIT101Database::class.java,
                "bit101.db"
            ).fallbackToDestructiveMigration().build()
        }
        val Context.dataStore:DataStore<Preferences> by preferencesDataStore(name = "settings")
    }
    override fun onCreate() {
        super.onCreate()
        context=applicationContext
    }

    override fun onTerminate() {
        super.onTerminate()
        DB.close()
    }
}