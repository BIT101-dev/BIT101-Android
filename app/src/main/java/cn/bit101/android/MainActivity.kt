package cn.bit101.android

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.ui.MainApp
import cn.bit101.android.ui.theme.BIT101Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
            SettingDataStore.settingRotate.flow.collect {
                requestedOrientation = if (it) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }
}

