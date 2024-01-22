package cn.bit101.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.umeng.commonsdk.UMConfigure
import dagger.hilt.android.HiltAndroidApp

/**
 * @author flwfdd
 * @date 2023/3/16 23:19
 * @description _(:з」∠)_
 */
@HiltAndroidApp
class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        // 友盟初始化
        UMConfigure.preInit(this, "64692214ba6a5259c455c4ed", "BIT101")
        UMConfigure.init(
            this,
            "64692214ba6a5259c455c4ed",
            "BIT101",
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
    }
}