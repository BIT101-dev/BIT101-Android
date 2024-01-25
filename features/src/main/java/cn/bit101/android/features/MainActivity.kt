package cn.bit101.android.features

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.bit101.android.config.setting.base.ThemeSettings
import cn.bit101.android.features.theme.BIT101Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeSettings: ThemeSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var loading = true

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loading = false
            }
        }

        splashScreen.setKeepOnScreenCondition { loading }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BIT101Theme {
                MainApp()
            }
        }

        // 设置屏幕旋转
        MainScope().launch {
            themeSettings.autoRotate.flow.collect {
                requestedOrientation = if (it) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }
}