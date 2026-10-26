package cn.bit101.android.features.schedule.ddl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.withSimpleErrorDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class DDLScheduleViewModel @Inject constructor(
    private val ddlScheduleRepo: DDLScheduleRepo,
    private val ddlSettings: DDLSettings,
    private val loginRepo: SchoolLoginRepo,
) : ViewModel() {
    private val subscriptionError = MutableStateFlow<String?>(null)
    private val eventsError = MutableStateFlow<String?>(null)
    val loadError = combine(subscriptionError, eventsError) { subscription, events -> subscription ?: events }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val _calendarUrl = MutableStateFlow<String?>(null)
    val lexueCalendarUrlFlow = _calendarUrl.asStateFlow()
    private var beforeDay = 7
    private val _events = MutableStateFlow<List<DDLScheduleEntity>>(emptyList())
    val events = _events.asStateFlow()
    private val _refreshState = MutableStateFlow<SimpleErrorDataState<Unit>?>(null)
    val refreshState = _refreshState.asStateFlow()
    private val _editState = MutableStateFlow<SimpleErrorDataState<String>?>(null)
    val editState = _editState.asStateFlow()
    private var eventsJob: Job? = null

    init {
        loadEvents()
        refreshCalendar()
    }

    fun loadEvents() {
        eventsJob?.cancel()
        subscriptionError.value = null
        eventsError.value = null
        _calendarUrl.value = null
        _events.value = emptyList()
        eventsJob = viewModelScope.launch {
            launch {
                try {
                    ddlScheduleRepo.calendarUrl.collect { _calendarUrl.value = it }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _calendarUrl.value = null
                    subscriptionError.value = "读取订阅设置失败，请重试"
                }
            }
            launch {
                try {
                    combine(ddlSettings.beforeDay.flow, ddlSettings.afterDay.flow) { before, after -> before to after }
                        .collectLatest { (before, after) ->
                            beforeDay = before.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
                            ddlScheduleRepo.getFutureDDL(LocalDateTime.now().minusDays(after.coerceIn(0, Int.MAX_VALUE.toLong())))
                                .collect(_events::emit)
                        }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _events.value = emptyList()
                    eventsError.value = "读取日程失败，请重试"
                }
            }
        }
    }

    fun refreshCalendar(acquireUrl: Boolean = false) = withSimpleErrorDataState(_refreshState) {
        if (acquireUrl) ddlScheduleRepo.refreshSubscription()
        if (ddlScheduleRepo.calendarUrl.first().isNotBlank()) {
            loginRepo.withSchoolSession { ddlScheduleRepo.syncCalendar() }
        }
    }

    private val editMutex = Mutex()
    private val pendingSubmissions = mutableSetOf<String>()

    // 不同修改排队执行；仅合并同一编辑窗口的重复提交，完成状态的每次点击都保留。
    private fun edit(message: String, key: String? = null, onSuccess: () -> Unit = {}, block: suspend () -> Unit): Job? {
        if (key != null && !pendingSubmissions.add(key)) return null
        return viewModelScope.launch {
            try {
                editMutex.withLock {
                    _editState.value = SimpleErrorDataState.Loading()
                    try {
                        block()
                        onSuccess()
                        _editState.value = SimpleErrorDataState.Success(message)
                    } catch (e: CancellationException) {
                        _editState.value = null
                        throw e
                    } catch (e: Exception) {
                        _editState.value = SimpleErrorDataState.Fail("保存日程失败，请重试")
                    }
                }
            } finally {
                key?.let(pendingSubmissions::remove)
            }
        }
    }

    fun setDone(event: DDLScheduleEntity, done: Boolean) = edit("已更新") {
        ddlScheduleRepo.setDone(event, done)
    }

    fun addDDL(title: String, time: LocalDateTime, text: String, onSuccess: () -> Unit = {}) =
        edit("添加成功", "add", onSuccess) {
            ddlScheduleRepo.insertDDL(DDLScheduleEntity(0, "main", UUID.randomUUID().toString(), title, text, time, false))
        }

    fun updateDDL(item: DDLScheduleEntity, title: String, time: LocalDateTime, text: String, onSuccess: () -> Unit = {}) =
        edit("修改成功", "update:${item.id}", onSuccess) { ddlScheduleRepo.updateDDL(item.copy(title = title, time = time, text = text)) }

    fun deleteDDL(item: DDLScheduleEntity, onSuccess: () -> Unit = {}) =
        edit("删除成功", "delete:${item.id}", onSuccess) { ddlScheduleRepo.deleteDDL(item) }

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
        val enoughTime = 1440L * beforeDay.coerceAtLeast(1)
        val now = LocalDateTime.now()
        val diff = now.until(time, java.time.temporal.ChronoUnit.MINUTES)
        if (diff <= 0) return 0f
        if (diff >= enoughTime) return 1f
        return diff.toFloat() / enoughTime
    }



}
