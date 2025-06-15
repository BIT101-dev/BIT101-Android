package cn.bit101.android.features.common.utils

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.provider.CalendarContract
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class ScheduleCreateInfo(
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val date: LocalDate,
    val beginTime: LocalTime,
    val endTime: LocalTime,
) {
    /**
     * 检验自定义日程参数是否合法
     */
    fun check(): Boolean {
        return endTime > beginTime
    }

    /**
     * 将自定义日程参数转换为数据库 Entity
     * 参数非法时抛出异常
     */
    fun toEntity(): CustomScheduleEntity {
        if(!check()) throw IllegalArgumentException("Invalid Period")

        return CustomScheduleEntity(
            id = 0,
            title = title,
            subtitle = subtitle,
            description = description,
            date = date,
            beginTime = beginTime,
            endTime = endTime,
        )
    }
}

// 把日程添加到系统日历中
fun addScheduleToSystemCalendar(
    context: Context,
    date: LocalDate,
    beginTime: LocalTime,
    endTime: LocalTime,
    title: String,
    description: String,
    location: String,
) {
    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, Calendar.getInstance().run {
            set(
                date.year,
                date.month.value - 1,
                date.dayOfMonth,
                beginTime.hour,
                beginTime.minute,
            )
            timeInMillis
        })
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, Calendar.getInstance().run {
            set(
                date.year,
                date.month.value - 1,
                date.dayOfMonth,
                endTime.hour,
                endTime.minute,
            )
            timeInMillis
        })
        .putExtra(CalendarContract.Events.TITLE, title)
        .putExtra(CalendarContract.Events.DESCRIPTION, description)
        .putExtra(CalendarContract.Events.EVENT_LOCATION, location)

    context.startActivity(intent)
}
fun addScheduleToSystemCalendar(context: Context, schedule: CustomScheduleEntity) {
    addScheduleToSystemCalendar(
        context = context,
        date = schedule.date,
        beginTime = schedule.beginTime,
        endTime = schedule.endTime,
        title = schedule.title,
        description = schedule.description,
        location = schedule.subtitle,
    )
}
fun addScheduleToSystemCalendar(context: Context, schedule: ExamScheduleEntity) {
    addScheduleToSystemCalendar(
        context = context,
        date = schedule.date,
        beginTime = schedule.beginTime,
        endTime = schedule.endTime,
        title = "[考试] ${schedule.name}",
        description = "座位号: ${schedule.seatId}\n考试时间: ${
            schedule.beginTime.until(
                schedule.endTime,
                ChronoUnit.MINUTES
            )
        } 分钟\n模式: ${schedule.examMode}",
        location = schedule.classroom,
    )
}