package cn.bit101.android

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Window
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.ui.MainApp
import cn.bit101.android.ui.setting2.SettingContent
import cn.bit101.android.ui.theme.BIT101Theme
import cn.bit101.android.utils.ColorUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        var window: Window? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        MainActivity.window = window

        setContent {
            BIT101Theme {
                // 设置导航栏颜色与应用内导航栏匹配
                window?.navigationBarColor =
                    MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
                        .toArgb()
//                MainApp()
                SettingContent()
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

