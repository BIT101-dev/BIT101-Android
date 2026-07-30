package cn.bit101.android

import android.app.Application
import com.umeng.commonsdk.UMConfigure
import dagger.hilt.android.HiltAndroidApp
import org.conscrypt.Conscrypt
import java.security.Security

/**
 * @author flwfdd
 * @date 2023/3/16 23:19
 * @description _(:з」∠)_
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 将打包的 Conscrypt 设为默认安全提供者，使旧系统支持 TLS 1.3
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

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