package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.withSimpleStateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.withSimpleErrorDataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
internal class DDLViewModel @Inject constructor(
    private val ddlScheduleRepo: DDLScheduleRepo,
    private val loginRepo: SchoolLoginRepo,
    private val ddlSettings: DDLSettings
) : ViewModel() {

    val beforeDayFlow = ddlSettings.beforeDay.flow

    val afterDayFlow = ddlSettings.afterDay.flow

    val updateLexueCalendarUrlStateLiveData = MutableLiveData<SimpleState?>()

    val updateLexueCalendarLiveData = MutableLiveData<SimpleState?>()

    private val _saveSettingsState = MutableStateFlow<SimpleErrorDataState<Unit>?>(null)
    val saveSettingsState = _saveSettingsState.asStateFlow()

    fun setBeforeDay(day: Long) {
        withSimpleErrorDataState(_saveSettingsState) {
            ddlSettings.beforeDay.set(day)
        }
    }

    fun setAfterDay(day: Long) {
        withSimpleErrorDataState(_saveSettingsState) {
            ddlSettings.afterDay.set(day)
        }
    }

    fun updateLexueCalendarUrl() = withSimpleStateLiveData(updateLexueCalendarUrlStateLiveData) {
        ddlScheduleRepo.refreshSubscription()
    }

    fun updateLexueCalendar() = withSimpleStateLiveData(updateLexueCalendarLiveData) {
        loginRepo.withSchoolSession { ddlScheduleRepo.syncCalendar() }
    }
}
