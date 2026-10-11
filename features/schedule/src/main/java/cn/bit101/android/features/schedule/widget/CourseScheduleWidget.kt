package cn.bit101.android.features.schedule.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.features.schedule.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private data class WidgetEntry(
    val id: Long,
    val title: String,
    val location: String,
    val time: String,
    val sortTime: LocalTime,
    val endTime: LocalTime?,
    val background: Int,
)

private data class WidgetSchedule(
    val date: LocalDate,
    val needsSync: Boolean,
    val entries: List<WidgetEntry>,
    val finished: Boolean,
)

@Singleton
class CourseScheduleWidget @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coursesRepo: CoursesRepo,
    private val settings: CourseScheduleSettings,
) {
    private val updateMutex = Mutex()
    private val manager get() = AppWidgetManager.getInstance(context)
    private val provider get() = ComponentName(context, CourseScheduleWidgetProvider::class.java)
    private val alarms get() = context.getSystemService(AlarmManager::class.java)
    private val refreshIntent
        get() = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, CourseScheduleWidgetProvider::class.java)
                .setAction(CourseScheduleWidgetProvider.ACTION_TIME_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    suspend fun observeChanges() {
        val settingChanges = combine(
            settings.term.flow,
            settings.firstDay.flow,
            settings.timeTable.flow,
            settings.showExamInfo.flow,
        ) { _, _, _, _ -> Unit }

        combine(
            coursesRepo.getCoursesFromLocal(),
            coursesRepo.getExamsFromLocal(),
            coursesRepo.getCustomSchedules(),
            settingChanges,
        ) { _, _, _, _ -> Unit }.conflate().collect { update() }
    }

    private suspend fun load(date: LocalDate, now: LocalDateTime): WidgetSchedule {
        val term = settings.term.get()
        val firstDay = settings.firstDay.get()
        val timeTable = settings.timeTable.get()
        val needsSync = term.isBlank() || firstDay == null
        val entries = mutableListOf<WidgetEntry>()

        if (term.isNotBlank() && firstDay != null && !date.isBefore(firstDay)) {
            val week = (ChronoUnit.DAYS.between(firstDay, date) / 7 + 1).toInt()
            coursesRepo.getCoursesFromLocal(term, week).first()
                .filter { it.weekday == date.dayOfWeek.value }
                .forEach { course ->
                    val start = timeTable.getOrNull(course.start_section - 1)?.startTime
                    val end = timeTable.getOrNull(course.end_section - 1)?.endTime
                    val time = when {
                        course.start_section <= 0 || course.end_section < course.start_section ->
                            context.getString(R.string.course_widget_unknown_time)

                        start != null && end != null -> timeRange(start, end)
                        else -> "${course.start_section}–${course.end_section}节"
                    }
                    entries += WidgetEntry(
                        id = course.id.toLong() * 3,
                        title = course.name,
                        location = course.classroom,
                        time = time,
                        sortTime = start ?: LocalTime.MAX,
                        endTime = end.takeIf { course.start_section > 0 && course.end_section >= course.start_section },
                        background = R.drawable.course_widget_course_background,
                    )
                }
        }

        if (term.isNotBlank() && settings.showExamInfo.get()) {
            coursesRepo.getExamsFromLocal(term).first()
                .filter { it.date == date }
                .forEach { exam ->
                    entries += WidgetEntry(
                        id = exam.id.toLong() * 3 + 1,
                        title = context.getString(R.string.course_widget_exam_title, exam.name),
                        location = exam.classroom,
                        time = timeRange(exam.beginTime, exam.endTime),
                        sortTime = exam.beginTime,
                        endTime = exam.endTime,
                        background = R.drawable.course_widget_exam_background,
                    )
                }
        }

        coursesRepo.getCustomSchedules().first()
            .filter { it.date == date }
            .forEach { schedule ->
                entries += WidgetEntry(
                    id = schedule.id.toLong() * 3 + 2,
                    title = schedule.title,
                    location = schedule.subtitle,
                    time = timeRange(schedule.beginTime, schedule.endTime),
                    sortTime = schedule.beginTime,
                    endTime = schedule.endTime,
                    background = R.drawable.course_widget_custom_background,
                )
            }

        // 时间未知的安排继续显示，避免把无法判断是否结束的课程隐藏掉
        val remaining = if (date == now.toLocalDate()) {
            entries.filter { it.endTime == null || it.endTime > now.toLocalTime() }
        } else entries

        return WidgetSchedule(
            date, needsSync, remaining.sortedBy { it.sortTime },
            finished = entries.isNotEmpty() && remaining.isEmpty(),
        )
    }

    internal suspend fun update(appWidgetIds: IntArray? = null) = withContext(Dispatchers.IO) {
        updateMutex.withLock {
            val installedIds = manager.getAppWidgetIds(provider)
            if (installedIds.isEmpty()) {
                alarms.cancel(refreshIntent)
                return@withLock
            }
            val ids = appWidgetIds ?: installedIds
            val now = LocalDateTime.now()
            val today = now.toLocalDate()
            var nextRefresh = today.plusDays(1).atStartOfDay()
            for (id in ids) {
                try {
                    val options = manager.getAppWidgetOptions(id)
                    val todaySchedule = load(today, now)
                    val nextEnd = todaySchedule.entries.mapNotNull { it.endTime }.minOrNull()
                    if (nextEnd != null && today.atTime(nextEnd) < nextRefresh) {
                        nextRefresh = today.atTime(nextEnd)
                    }
                    val selectedOffset = if (options.getString(OPTION_SELECTION_DATE) == today.toString()) {
                        options.getInt(OPTION_DAY_OFFSET).coerceIn(0, 1)
                    } else null
                    val tomorrowSchedule = if (selectedOffset == 1 ||
                        selectedOffset == null && todaySchedule.entries.isEmpty()
                    ) load(today.plusDays(1), now) else null
                    val dayOffset = selectedOffset ?: if (tomorrowSchedule?.entries?.isNotEmpty() == true) 1 else 0
                    val schedule = if (dayOffset == 1) checkNotNull(tomorrowSchedule) else todaySchedule
                    val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        RemoteViews(
                            mapOf(
                                SizeF(130f, 110f) to createViews(id, dayOffset, schedule, compact = true),
                                SizeF(240f, 110f) to createViews(id, dayOffset, schedule, compact = false),
                            )
                        )
                    } else {
                        createViews(
                            id, dayOffset, schedule,
                            compact = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < 240,
                        )
                    }
                    manager.updateAppWidget(id, views)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        @Suppress("DEPRECATION")
                        manager.notifyAppWidgetViewDataChanged(id, R.id.course_widget_list)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "刷新课表小组件 $id 失败", e)
                    showReadFailure(id)
                }
            }
            alarms.setWindow(
                AlarmManager.RTC,
                nextRefresh.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                10 * 60 * 1_000L,
                refreshIntent,
            )
        }
    }

    private fun createViews(
        id: Int,
        dayOffset: Int,
        schedule: WidgetSchedule,
        compact: Boolean,
    ): RemoteViews = baseViews(id, dayOffset).apply {
        val weekdays = context.resources.getStringArray(R.array.course_widget_weekdays)
        setTextViewText(
            R.id.course_widget_summary,
            context.getString(
                if (dayOffset == 0) R.string.course_widget_remaining_summary else R.string.course_widget_summary,
                weekdays[schedule.date.dayOfWeek.value - 1], schedule.entries.size,
            ),
        )
        setTextViewText(
            R.id.course_widget_empty,
            context.getString(
                when {
                    schedule.needsSync -> R.string.course_widget_sync_required
                    schedule.finished -> R.string.course_widget_finished_day
                    else -> R.string.course_widget_free_day
                },
            ),
        )
        setEmptyView(R.id.course_widget_list, R.id.course_widget_empty)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val items = RemoteViews.RemoteCollectionItems.Builder()
                .setHasStableIds(true)
                .setViewTypeCount(1)
            schedule.entries.forEach { items.addItem(it.id, entryViews(it, compact)) }
            setRemoteAdapter(R.id.course_widget_list, items.build())
        } else {
            val intent = Intent(context, CourseScheduleWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                putExtra(EXTRA_COMPACT, compact)
                putExtra(CourseScheduleWidgetProvider.EXTRA_DAY_OFFSET, dayOffset)
                data = Uri.parse("bit101-widget://list/$id/$dayOffset/$compact")
            }
            @Suppress("DEPRECATION")
            setRemoteAdapter(R.id.course_widget_list, intent)
        }
    }

    private fun baseViews(id: Int, dayOffset: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.course_schedule_widget).apply {
            val dayLabel = context.getString(
                if (dayOffset == 0) R.string.course_widget_today else R.string.course_widget_tomorrow,
            )
            setTextViewText(R.id.course_widget_day, dayLabel)
            setContentDescription(
                R.id.course_widget_day,
                context.getString(
                    if (dayOffset == 0) R.string.course_widget_show_tomorrow
                    else R.string.course_widget_show_today,
                ),
            )
            setOnClickPendingIntent(
                R.id.course_widget_day,
                broadcastIntent(id, CourseScheduleWidgetProvider.ACTION_TOGGLE_DAY, dayOffset),
            )
            setOnClickPendingIntent(
                R.id.course_widget_refresh,
                broadcastIntent(id, CourseScheduleWidgetProvider.ACTION_REFRESH),
            )

            val launch = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName))
                .setAction(CourseScheduleWidgetProvider.ACTION_OPEN_SCHEDULE)
                .setData(Uri.parse("bit101-widget://open/$id/$dayOffset"))
                .putExtra(CourseScheduleWidgetProvider.EXTRA_DAY_OFFSET, dayOffset)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val open = PendingIntent.getActivity(
                context, id, launch,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            setOnClickPendingIntent(R.id.course_widget_summary, open)
            setOnClickPendingIntent(R.id.course_widget_empty, open)
            val mutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            setPendingIntentTemplate(
                R.id.course_widget_list,
                PendingIntent.getActivity(context, id + 1, launch, PendingIntent.FLAG_UPDATE_CURRENT or mutable),
            )
        }

    private fun entryViews(entry: WidgetEntry, compact: Boolean): RemoteViews =
        RemoteViews(
            context.packageName,
            if (compact) R.layout.course_widget_row_compact else R.layout.course_widget_row_wide,
        ).apply {
            setTextViewText(R.id.course_widget_title, entry.title)
            setTextViewText(R.id.course_widget_time, if (compact) entry.time else entry.time.replace('–', '\n'))
            setTextViewText(R.id.course_widget_location, entry.location)
            setViewVisibility(R.id.course_widget_location, if (entry.location.isBlank()) View.GONE else View.VISIBLE)
            setInt(R.id.course_widget_card, "setBackgroundResource", entry.background)
            setContentDescription(
                R.id.course_widget_card,
                listOf(entry.title, entry.time, entry.location).filter { it.isNotBlank() }.joinToString("，"),
            )
            setOnClickFillInIntent(R.id.course_widget_card, Intent())
        }

    private fun showReadFailure(id: Int) {
        val dayOffset = manager.getAppWidgetOptions(id).getInt(OPTION_DAY_OFFSET).coerceIn(0, 1)
        manager.updateAppWidget(id, baseViews(id, dayOffset).apply {
            setTextViewText(R.id.course_widget_summary, context.getString(R.string.course_widget_name))
            setViewVisibility(R.id.course_widget_list, View.GONE)
            setViewVisibility(R.id.course_widget_empty, View.VISIBLE)
            setTextViewText(R.id.course_widget_empty, context.getString(R.string.course_widget_read_failed))
        })
    }

    internal fun createFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val compact = intent.getBooleanExtra(EXTRA_COMPACT, true)
        return object : RemoteViewsService.RemoteViewsFactory {
            private var entries = emptyList<WidgetEntry>()

            override fun onCreate() = Unit

            override fun onDataSetChanged() {
                try {
                    val now = LocalDateTime.now()
                    val dayOffset = intent.getIntExtra(CourseScheduleWidgetProvider.EXTRA_DAY_OFFSET, 0).coerceIn(0, 1)
                    entries = runBlocking { load(now.toLocalDate().plusDays(dayOffset.toLong()), now).entries }
                } catch (e: Exception) {
                    Log.e(TAG, "读取课表小组件 $id 失败", e)
                    entries = emptyList()
                    showReadFailure(id)
                }
            }

            override fun onDestroy() {
                entries = emptyList()
            }

            override fun getCount() = entries.size
            override fun getViewAt(position: Int): RemoteViews? =
                entries.getOrNull(position)?.let { entryViews(it, compact) }

            override fun getLoadingView(): RemoteViews? = null
            override fun getViewTypeCount() = 1
            override fun getItemId(position: Int) = entries[position].id
            override fun hasStableIds() = true
        }
    }

    private fun broadcastIntent(id: Int, action: String, dayOffset: Int = 0): PendingIntent =
        PendingIntent.getBroadcast(
            context, id,
            Intent(context, CourseScheduleWidgetProvider::class.java)
                .setAction(action)
                .setData(Uri.parse("bit101-widget://action/$id"))
                .putExtra(CourseScheduleWidgetProvider.EXTRA_DAY_OFFSET, dayOffset)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun timeRange(start: LocalTime, end: LocalTime) =
        "${start.format(TIME_FORMAT)}–${end.format(TIME_FORMAT)}"

    companion object {
        internal const val OPTION_DAY_OFFSET = "cn.bit101.android.widget.DAY_OFFSET"
        internal const val OPTION_SELECTION_DATE = "cn.bit101.android.widget.SELECTION_DATE"
        private const val EXTRA_COMPACT = "cn.bit101.android.widget.COMPACT"
        private const val TAG = "CourseScheduleWidget"
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

        fun requestPin(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            if (!manager.isRequestPinAppWidgetSupported) return false
            val extras = Bundle().apply {
                putParcelable(
                    AppWidgetManager.EXTRA_APPWIDGET_PREVIEW,
                    RemoteViews(context.packageName, R.layout.course_widget_preview),
                )
            }
            return manager.requestPinAppWidget(
                ComponentName(context, CourseScheduleWidgetProvider::class.java), extras, null,
            )
        }
    }
}
