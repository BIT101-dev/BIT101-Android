package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.data.repo.base.FreeClassroomRepo
import cn.bit101.android.data.repo.base.LoginRepo
import cn.bit101.android.features.common.helper.*
import cn.bit101.api.model.common.CampusInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal data class FreeClassroomSettingData(
    val currentCampus: CampusInfo,
    val hideBusyClassroom: Boolean,
    val freeMinutesThreshold: Long,
) {
    companion object {
        val default = FreeClassroomSettingData(
            currentCampus = CampusInfo("", ""),
            hideBusyClassroom = true,
            freeMinutesThreshold = 5L,
        )
    }
}

@HiltViewModel
internal class FreeClassroomViewModel @Inject constructor(
    private val freeClassroomSettings: FreeClassroomSettings,
    private val freeClassroomRepo: FreeClassroomRepo,
    private val loginRepo: LoginRepo,
) : ViewModel() {
    val settingDataFlow = combine(
        freeClassroomSettings.currentCampusCode.flow,
        freeClassroomSettings.currentCampusDisplayName.flow,
    ) { settings ->
        FreeClassroomSettingData(
            currentCampus = CampusInfo(
                code = settings[0],
                displayName = settings[1],
            ),
            hideBusyClassroom = freeClassroomSettings.hideBusyClassroom.get(),
            freeMinutesThreshold = freeClassroomSettings.freeMinutesThreshold.get(),
        )
    }

    val changeSettingStatusLiveData = MutableLiveData<SimpleState?>(null)

    fun setCampus(campus: CampusInfo) = withSimpleStateLiveData(changeSettingStatusLiveData) {
        freeClassroomSettings.currentCampusDisplayName.set(campus.displayName)
        freeClassroomSettings.currentCampusCode.set(campus.code)
    }
    fun setFreeMinutesThreshold(freeMinutesThreshold: Long) = withSimpleStateLiveData(changeSettingStatusLiveData) {
        freeClassroomSettings.freeMinutesThreshold.set(freeMinutesThreshold)
    }

    fun setSettingData(settingData: FreeClassroomSettingData) = withScope {
        freeClassroomSettings.currentCampusCode.set(settingData.currentCampus.code)
        freeClassroomSettings.currentCampusDisplayName.set(settingData.currentCampus.displayName)
        freeClassroomSettings.hideBusyClassroom.set(settingData.hideBusyClassroom)
        freeClassroomSettings.freeMinutesThreshold.set(settingData.freeMinutesThreshold)
    }

    val getBuildingTypeStatusLiveData = MutableLiveData<SimpleDataState<List<CampusInfo>>?>(null)

    fun loadCampusInfos() = withSimpleDataStateLiveData(getBuildingTypeStatusLiveData) {
        loginRepo.doOperationRequiresLogin(freeClassroomRepo::getCampusInfos)
    }
}