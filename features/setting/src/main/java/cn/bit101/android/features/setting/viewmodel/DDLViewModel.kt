package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.repo.EclassDdlSyncer
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.data.repo.base.EclassRepo
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DDLViewModel @Inject constructor(
    private val ddlScheduleRepo: DDLScheduleRepo,
    private val loginRepo: LoginRepo,
    private val ddlSettings: DDLSettings,
    /** 课程中心（eclass）作业同步器 —— 与 DDL 页共用同一份合并规则。 */
    private val eclassDdlSyncer: EclassDdlSyncer,
    private val eclassRepo: EclassRepo,
) : ViewModel() {

    val beforeDayFlow = ddlSettings.beforeDay.flow

    val afterDayFlow = ddlSettings.afterDay.flow

    val updateLexueCalendarUrlStateLiveData = MutableLiveData<SimpleState?>()

    val updateLexueCalendarLiveData = MutableLiveData<SimpleState?>()

    val updateEclassDdlStateLiveData = MutableLiveData<SimpleState?>()

    /** 课程中心（eclass）会话是否可用：`null` = 还在检查。 */
    private val _eclassSessionAlive = MutableStateFlow<Boolean?>(null)
    val eclassSessionAlive: StateFlow<Boolean?> = _eclassSessionAlive.asStateFlow()

    init {
        checkEclassSession()
    }

    /** 重新检查课程中心会话 —— 用户可能刚在 WebView 里登录完回来。 */
    fun checkEclassSession() {
        viewModelScope.launch {
            _eclassSessionAlive.value = null
            _eclassSessionAlive.value =
                runCatching { eclassRepo.isSessionAlive() }.getOrDefault(false)
        }
    }

    /**
     * 重新拉取课程中心（eclass）作业（设置页动作项）。
     *
     * 同步直接写 Room，DDL 页的 `events` 是 Room 流 —— 写库后那边自动刷新。
     */
    fun updateEclassDdl() = withSimpleStateLiveData(updateEclassDdlStateLiveData) {
        eclassDdlSyncer.sync()
    }

    fun setBeforeDay(day: Long) {
        viewModelScope.launch {
            ddlSettings.beforeDay.set(day)
        }
    }

    fun setAfterDay(day: Long) {
        viewModelScope.launch {
            ddlSettings.afterDay.set(day)
        }
    }

    private suspend fun updateLexueCalendarUrlWithoutState() {
        val url = ddlScheduleRepo.getCalendarUrl() ?: throw Exception("url is null")
        ddlSettings.url.set(url)
    }

    // 从网络获取日程url 返回是否成功
    fun updateLexueCalendarUrl() = withSimpleStateLiveData(updateLexueCalendarUrlStateLiveData) {
        loginRepo.doOperationRequiresLogin(this::updateLexueCalendarUrlWithoutState)
    }

    private suspend fun updateLexueCalendarWithoutState() {
        val url = ddlSettings.url.get()
        val events = ddlScheduleRepo.getCalendarFromNet(url)

        val UIDs = events.map { it.uid }
        // 获取数据库中已有日程
        val existItems = HashMap<String, DDLScheduleEntity>()
        ddlScheduleRepo.getCalendarFromLocal(UIDs).forEach { existItems[it.uid] = it }
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
    }

    // 从网络获取日程
    fun updateLexueCalendar() = withSimpleStateLiveData(updateLexueCalendarLiveData) {
        loginRepo.doOperationRequiresLogin(this::updateLexueCalendarWithoutState)
    }
}