package cn.bit101.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import cn.bit101.android.database.BIT101Database
import com.umeng.commonsdk.UMConfigure
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * @author flwfdd
 * @date 2023/3/16 23:19
 * @description _(:з」∠)_
 */
@HiltAndroidApp
class App : Application() {

//    @Inject lateinit var database: BIT101Database

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


        // 用于WebView上传文件
        lateinit var activityResultLauncher: ActivityResultLauncher<String>
        val activityResult = MutableStateFlow(listOf(Uri.EMPTY))

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

    override fun onTerminate() {
        super.onTerminate()
//        database.close()
    }
}