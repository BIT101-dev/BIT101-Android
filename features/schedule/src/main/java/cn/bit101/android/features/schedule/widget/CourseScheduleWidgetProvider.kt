package cn.bit101.android.features.schedule.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
open class CourseScheduleWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var widget: CourseScheduleWidget

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_DAY, ACTION_SELECT_DAY, ACTION_REFRESH -> {
                val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val manager = AppWidgetManager.getInstance(context)
                if (manager.getAppWidgetInfo(id)?.provider != ComponentName(context, javaClass)) return
                if (intent.action == ACTION_REFRESH) {
                    updateAsync(intArrayOf(id))
                } else {
                    val options = manager.getAppWidgetOptions(id)
                    val offset = intent.getIntExtra(EXTRA_DAY_OFFSET, 0).coerceIn(0, 1)
                    options.putInt(
                        CourseScheduleWidget.OPTION_DAY_OFFSET,
                        if (intent.action == ACTION_TOGGLE_DAY) 1 - offset else offset,
                    )
                    options.putString(CourseScheduleWidget.OPTION_SELECTION_DATE, LocalDate.now().toString())
                    manager.updateAppWidgetOptions(id, options)
                }
            }

            ACTION_TIME_REFRESH, Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED -> updateAsync()
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAsync(appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        updateAsync(intArrayOf(appWidgetId))
    }

    override fun onDisabled(context: Context) {
        updateAsync()
    }

    private fun updateAsync(ids: IntArray? = null) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 留出时间结束广播，避免超时
                withTimeout(8_000) { widget.update(ids) }
            } catch (e: Exception) {
                Log.e("CourseWidgetProvider", "刷新课表小组件失败", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        internal const val ACTION_TOGGLE_DAY = "cn.bit101.android.widget.TOGGLE_DAY"
        internal const val ACTION_SELECT_DAY = "cn.bit101.android.widget.SELECT_DAY"
        internal const val ACTION_REFRESH = "cn.bit101.android.widget.REFRESH"
        internal const val ACTION_TIME_REFRESH = "cn.bit101.android.widget.TIME_REFRESH"
        const val ACTION_OPEN_SCHEDULE = "cn.bit101.android.widget.OPEN_SCHEDULE"
        const val EXTRA_DAY_OFFSET = "cn.bit101.android.widget.OPEN_DAY_OFFSET"
    }
}

class CourseScheduleWideWidgetProvider : CourseScheduleWidgetProvider()
