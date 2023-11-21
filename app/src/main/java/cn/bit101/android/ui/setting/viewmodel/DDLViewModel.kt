package cn.bit101.android.ui.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.database.entity.DDLScheduleEntity
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.repo.base.DDLScheduleRepo
import cn.bit101.android.ui.gallery.common.SimpleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DDLViewModel @Inject constructor(
    private val ddlScheduleRepo: DDLScheduleRepo,
) : ViewModel() {

    val beforeDayFlow = SettingDataStore.ddlScheduleBeforeDay.flow

    val afterDayFlow = SettingDataStore.ddlScheduleAfterDay.flow


    val updateLexueCalendarUrlStateLiveData = MutableLiveData<SimpleState>()

    val updateLexueCalendarLiveData = MutableLiveData<SimpleState>()


    fun setBeforeDay(day: Long) {
        viewModelScope.launch {
            SettingDataStore.ddlScheduleBeforeDay.set(day)
        }
    }

    fun setAfterDay(day: Long) {
        viewModelScope.launch {
            SettingDataStore.ddlScheduleAfterDay.set(day)
        }
    }

    // 从网络获取日程url 返回是否成功
    fun updateLexueCalendarUrl() {
        updateLexueCalendarUrlStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = ddlScheduleRepo.getCalendarUrl() ?: throw Exception("url is null")
                SettingDataStore.lexueCalendarUrl.set(url)
                updateLexueCalendarUrlStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                updateLexueCalendarUrlStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    // 从网络获取日程
    fun updateLexueCalendar() {
        updateLexueCalendarLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = SettingDataStore.lexueCalendarUrl.get()
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
                updateLexueCalendarLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                updateLexueCalendarLiveData.postValue(SimpleState.Fail)
            }
        }
    }
}