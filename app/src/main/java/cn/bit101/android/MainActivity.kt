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
import cn.bit101.android.status.base.LoginStatusManager
import cn.bit101.android.ui.MainApp
import cn.bit101.android.ui.theme.BIT101Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var loginStatusManager: LoginStatusManager

    companion object {
        var window: Window? = null
    }

    // 判断一个颜色是否是浅色
    private fun isLightColor(c: Color): Boolean {
        val color = c.toArgb()
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color shr 0 and 0xFF
        val grayLevel = 0.2126 * red + 0.7152 * green + 0.0722 * blue
        return grayLevel >= 192
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.window = window

        setContent {
            BIT101Theme {


                // 设置状态栏文字颜色
                WindowCompat.getInsetsController(
                    window,
                    LocalView.current
                ).isAppearanceLightStatusBars = isLightColor(MaterialTheme.colorScheme.background)

                // 设置导航栏颜色与应用内导航栏匹配
                window?.navigationBarColor =
                    MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
                        .toArgb()
                MainApp()
            }
        }

        // 打开文件选择窗口
        App.activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
                MainScope().launch {
                    // 发送文件选择结果
                    App.activityResult.emit(it)
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

        MainScope().launch {
            // 更新乐学日程

        }

    }
}

