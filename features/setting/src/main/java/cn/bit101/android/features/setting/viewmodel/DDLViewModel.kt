package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DDLViewModel @Inject constructor(
    private val ddlScheduleRepo: DDLScheduleRepo,
    private val ddlSettings: DDLSettings
) : ViewModel() {

    val beforeDayFlow = ddlSettings.beforeDay.flow

    val afterDayFlow = ddlSettings.afterDay.flow

    val updateLexueCalendarUrlStateLiveData = MutableLiveData<SimpleState?>()

    val updateLexueCalendarLiveData = MutableLiveData<SimpleState?>()

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

    // 从网络获取日程url 返回是否成功
    fun updateLexueCalendarUrl() = withSimpleStateLiveData(updateLexueCalendarUrlStateLiveData) {
        val url = ddlScheduleRepo.getCalendarUrl() ?: throw Exception("url is null")
        ddlSettings.url.set(url)
    }

    // 从网络获取日程
    fun updateLexueCalendar() = withSimpleStateLiveData(updateLexueCalendarLiveData) {
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
}