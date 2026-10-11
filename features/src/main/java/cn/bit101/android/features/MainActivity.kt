package cn.bit101.android.features

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.bit101.android.config.setting.base.ThemeSettings
import cn.bit101.android.features.schedule.widget.CourseScheduleWidget
import cn.bit101.android.features.schedule.widget.CourseScheduleWidgetProvider
import cn.bit101.android.features.theme.BIT101Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeSettings: ThemeSettings

    @Inject
    lateinit var courseScheduleWidget: CourseScheduleWidget

    private var requestedScheduleDate by mutableStateOf<LocalDate?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        readWidgetIntent(intent)

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
                MainApp(
                    requestedScheduleDate = requestedScheduleDate,
                    onScheduleDateHandled = { requestedScheduleDate = null },
                )
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                courseScheduleWidget.observeChanges()
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readWidgetIntent(intent)
    }

    private fun readWidgetIntent(intent: Intent) {
        if (intent.action == CourseScheduleWidgetProvider.ACTION_OPEN_SCHEDULE) {
            val offset = intent.getIntExtra(CourseScheduleWidgetProvider.EXTRA_DAY_OFFSET, 0).coerceIn(0, 1)
            requestedScheduleDate = LocalDate.now().plusDays(offset.toLong())
        }
    }
}
