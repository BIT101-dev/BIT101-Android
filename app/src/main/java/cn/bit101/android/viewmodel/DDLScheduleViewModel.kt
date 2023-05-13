package cn.bit101.android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.App
import cn.bit101.android.database.DDLScheduleEntity
import cn.bit101.android.database.DataStore
import cn.bit101.android.net.school.getCalendar
import cn.bit101.android.net.school.getCalendarUrl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * @author flwfdd
 * @date 12/05/2023 23:00
 * @description _(:з」∠)_
 */

class DDLScheduleViewModel : ViewModel() {
    val lexueCalendarUrlFlow = DataStore.lexueCalendarUrlFlow
    var beforeDay = 7
    var afterDay = 3

    private val _events = MutableStateFlow<List<DDLScheduleEntity>>(emptyList())
    val events: StateFlow<List<DDLScheduleEntity>> = _events.asStateFlow()

    private var job: Job? = null

    init {
        viewModelScope.launch {
            DataStore.DDLScheduleBeforeDayFlow.collect {
                beforeDay = it.toInt()
            }
        }

        viewModelScope.launch {
            DataStore.DDLScheduleAfterDayFlow.collect {
                afterDay = it.toInt()
                startGetEvents(it)
            }
        }
    }

    // 设置颜色改变天数
    fun setBeforeDay(day: Long) {
        viewModelScope.launch {
            DataStore.setLong(DataStore.DDL_SCHEDULE_BEFORE_DAY, day)
        }
    }

    // 设置滞留天数
    fun setAfterDay(day: Long) {
        viewModelScope.launch {
            DataStore.setLong(DataStore.DDL_SCHEDULE_AFTER_DAY, day)
        }
    }

    // 获取日历
    private fun startGetEvents(day: Long) {
        job?.cancel()
        job = viewModelScope.launch {
            App.DB.DDLScheduleDao().getFuture(LocalDateTime.now().minusDays(day))
                .collect(
                    _events::emit
                )
        }
    }

    // 设置完成状态
    fun setDone(event: DDLScheduleEntity, done: Boolean) {
        viewModelScope.launch {
            App.DB.DDLScheduleDao().update(event.copy(done = done))
        }
    }

    // 获取和当前时间差的表述字符串
    fun remainTime(time: LocalDateTime): String {
        val now = LocalDateTime.now()
        var diff = now.until(time, java.time.temporal.ChronoUnit.MINUTES)
        var s = ""
        s += if (diff < 0) {
            "已过 "
        } else
            "剩余 "
        diff = Math.abs(diff)
        val day = diff / 1440
        val hour = (diff % 1440) / 60
        val minute = diff % 60
        s += if (day > 0) {
            "${day}天 ${hour}小时 ${minute}分钟"
        } else if (hour > 0) {
            "${hour}小时 ${minute}分钟"
        } else {
            "${minute}分钟"
        }
        return s
    }


    // 剩余时间比例 0~1 过期为0 超过ENOUGH_TIME为1
    fun remainTimeRatio(time: LocalDateTime): Float {
        val enoughTime = 1440 * beforeDay
        val now = LocalDateTime.now()
        val diff = now.until(time, java.time.temporal.ChronoUnit.MINUTES)
        if (diff <= 0) return 0f
        if (diff >= enoughTime) return 1f
        return diff.toFloat() / enoughTime
    }
}


// 从网络获取日程url 返回是否成功
suspend fun updateLexueCalendarUrl(): Boolean {
    try {
        val url = getCalendarUrl()
        if (url == null) {
            Log.e("DDLScheduleViewModel", "get lexue calendar url error")
            return false
        }
        DataStore.setString(DataStore.LEXUE_CALENDAR_URL, url)
        return true
    } catch (e: Exception) {
        Log.e("DDLScheduleViewModel", "get lexue calendar url error", e)
        return false
    }

}

// 从网络获取日程 返回是否成功
suspend fun updateLexueCalendar(): Boolean {
    try {
        val url = DataStore.lexueCalendarUrlFlow.first()
        if (url == null) {
            Log.e("DDLScheduleViewModel", "no lexue calendar url")
            return false
        }
        val events = getCalendar(url)
        val UIDs = events.map { it.uid }
        // 获取数据库中已有日程
        val existItems = HashMap<String, DDLScheduleEntity>()
        App.DB.DDLScheduleDao().getUIDs(UIDs).forEach { existItems[it.uid] = it }
        events.forEach {
            val item = DDLScheduleEntity(
                id = 0,
                uid = it.uid,
                group = "lexue",
                title = it.event,
                text = it.course + "\n\n" + it.description,
                time = it.time,
                done = false
            )
            if (existItems[it.uid] == null) {
                App.DB.DDLScheduleDao().insert(item)
            } else {
                App.DB.DDLScheduleDao().update(item.copy(done = existItems[it.uid]!!.done))
            }
        }
        return true
    } catch (e: Exception) {
        Log.e("DDLScheduleViewModel", "get lexue calendar error", e)
        return false
    }
}