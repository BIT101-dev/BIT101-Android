package cn.bit101.android.features.schedule.ddl

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.features.common.helper.withScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * @author flwfdd
 * @date 12/05/2023 23:00
 * @description _(:з」∠)_
 */

@HiltViewModel
internal class DDLScheduleViewModel @Inject constructor(
    private val ddlScheduleRepo: DDLScheduleRepo,
    private val ddlSettings: DDLSettings
) : ViewModel() {
    val lexueCalendarUrlFlow = ddlSettings.url.flow
    var beforeDay = 7
    var afterDay = 3

    private val _events = MutableStateFlow<List<DDLScheduleEntity>>(emptyList())
    val events: StateFlow<List<DDLScheduleEntity>> = _events.asStateFlow()

    private var job: Job? = null

    init {

        withScope {
            beforeDay = ddlSettings.beforeDay.get().toInt()
        }

        withScope {
            val afterDayLong = ddlSettings.afterDay.get()
            afterDay = afterDayLong.toInt()
            startGetEvents(afterDayLong)
        }

        // 更新日程
        withScope {
            updateLexueCalendar()
        }
    }

    // 获取日历
    private fun startGetEvents(day: Long) {
        job?.cancel()
        job = viewModelScope.launch {
            ddlScheduleRepo.getFutureDDL(LocalDateTime.now().minusDays(day))
                .collect(_events::emit)
        }
    }

    // 设置完成状态
    fun setDone(event: DDLScheduleEntity, done: Boolean) {
        viewModelScope.launch {
            ddlScheduleRepo.updateDDL(event.copy(done = done))
        }
    }

    // 添加DDL
    fun addDDL(
        title: String,
        time: LocalDateTime,
        text: String,
        group: String = "main"
    ) {
        val item = DDLScheduleEntity(
            id = 0,
            uid = UUID.randomUUID().toString(),
            group = group,
            title = title,
            text = text,
            time = time,
            done = false
        )
        viewModelScope.launch {
            ddlScheduleRepo.insertDDL(item)
        }
    }

    // 更新DDL
    fun updateDDL(
        item: DDLScheduleEntity,
        title: String,
        time: LocalDateTime,
        text: String,
    ) {
        viewModelScope.launch {
            ddlScheduleRepo.updateDDL(
                item.copy(
                    title = title,
                    time = time,
                    text = text
                )
            )
        }
    }

    // 删除DDL
    fun deleteDDL(item: DDLScheduleEntity) {
        viewModelScope.launch {
            ddlScheduleRepo.deleteDDL(item)
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



    // 从网络获取日程url 返回是否成功
    suspend fun updateLexueCalendarUrl(): Boolean {
        try {
            val url = ddlScheduleRepo.getCalendarUrl()
            if (url == null) {
                Log.e("DDLScheduleViewModel", "get lexue calendar url error")
                return false
            }
            ddlSettings.url.set(url)

            return true
        } catch (e: Exception) {
            Log.e("DDLScheduleViewModel", "get lexue calendar url error", e)
            return false
        }

    }

    // 从网络获取日程 返回是否成功
    suspend fun updateLexueCalendar(): Boolean {
        try {
            val url = ddlSettings.url.get()
            if (url.isEmpty()) {
                Log.e("DDLScheduleViewModel", "no lexue calendar url")
                return false
            }
            val events = ddlScheduleRepo.getCalendarFromNet(url)
            val UIDs = events.map { it.uid }
            // 获取数据库中已有日程
            val existItems = HashMap<String, DDLScheduleEntity>()
            ddlScheduleRepo.getDDLByUIDs(UIDs).forEach { existItems[it.uid] = it }
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
                    // 不存在则插入
                    ddlScheduleRepo.insertDDL(item)
                } else {
                    // 存在则更新
                    ddlScheduleRepo.updateDDL(
                        item.copy(
                            id = existItems[it.uid]!!.id,
                            done = existItems[it.uid]!!.done
                        )
                    )
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("DDLScheduleViewModel", "get lexue calendar error", e)
            return false
        }
    }
}