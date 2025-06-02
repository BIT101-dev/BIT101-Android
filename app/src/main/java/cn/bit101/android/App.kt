package cn.bit101.android

import android.app.Application
import com.umeng.commonsdk.UMConfigure
import dagger.hilt.android.HiltAndroidApp

/**
 * @author flwfdd
 * @date 2023/3/16 23:19
 * @description _(:з」∠)_
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

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