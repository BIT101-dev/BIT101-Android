package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.data.repo.base.FreeClassroomRepo
import cn.bit101.android.features.common.helper.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal data class FreeClassroomSettingData(
    val currentCampus: String,
    val hideBusyClassroom: Boolean,
    val freeMinutesThreshold: Long,
) {
    companion object {
        val default = FreeClassroomSettingData(
            currentCampus = "",
            hideBusyClassroom = true,
            freeMinutesThreshold = 5L,
        )
    }
}

@HiltViewModel
internal class FreeClassroomViewModel @Inject constructor(
    private val freeClassroomSettings: FreeClassroomSettings,
    private val freeClassroomRepo: FreeClassroomRepo
) : ViewModel() {
    val settingDataFlow = combine(
        freeClassroomSettings.currentCampus.flow,
    ) { settings ->
        FreeClassroomSettingData(
            currentCampus = settings[0],
            hideBusyClassroom = freeClassroomSettings.hideBusyClassroom.get(),
            freeMinutesThreshold = freeClassroomSettings.freeMinutesThreshold.get(),
        )
    }

    val changeSettingStatusLiveData = MutableLiveData<SimpleState?>(null)

    fun setCampus(campus: String) = withSimpleStateLiveData(changeSettingStatusLiveData) {
        freeClassroomSettings.currentCampus.set(campus)
    }
    fun setFreeMinutesThreshold(freeMinutesThreshold: Long) = withSimpleStateLiveData(changeSettingStatusLiveData) {
        freeClassroomSettings.freeMinutesThreshold.set(freeMinutesThreshold)
    }

    fun setSettingData(settingData: FreeClassroomSettingData) = withScope {
        freeClassroomSettings.currentCampus.set(settingData.currentCampus)
        freeClassroomSettings.hideBusyClassroom.set(settingData.hideBusyClassroom)
        freeClassroomSettings.freeMinutesThreshold.set(settingData.freeMinutesThreshold)
    }

    val getBuildingTypeStatusLiveData = MutableLiveData<SimpleDataState<List<String>>?>(null)

    fun loadBuildingTypes() = withSimpleDataStateLiveData(getBuildingTypeStatusLiveData) {
        freeClassroomRepo.getBuildingInfos().map{it.campusName}.distinct()
    }
}