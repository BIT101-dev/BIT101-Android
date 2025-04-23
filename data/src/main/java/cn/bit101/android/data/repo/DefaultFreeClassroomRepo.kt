package cn.bit101.android.data.repo

import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.data.database.BIT101Database
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.FreeClassroomRepo
import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.ClassroomInfo
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject

internal class DefaultFreeClassroomRepo @Inject constructor(
    private val database: BIT101Database,
    private val apiManager: APIManager,
    private val freeClassroomSettings: FreeClassroomSettings,
    private val scheduleSettings: CourseScheduleSettings
): FreeClassroomRepo{
    val api = apiManager.api

    override suspend fun getBuildingInfos(campusId: Int?): List<BuildingInfo> {
        api.schoolJxzxehallapp.getAppConfig()   // 不加这个会出奇怪的错误
        api.schoolJxzxehallapp.switchLang()

        val responseBody = api.schoolJxzxehallapp.getBuildingTypes(campusId).body() ?: throw IOException("get buildingTypes error")

        return responseBody.datas.cxjxl.rows
    }

    override suspend fun getClassroomInfos(buildingId: String): List<ClassroomInfo> {
        api.schoolJxzxehallapp.getAppConfig()
        api.schoolJxzxehallapp.switchLang()

        val responseBody = api.schoolJxzxehallapp.getClassroomData(
            buildingId = buildingId,
            termCode = scheduleSettings.term.get(),
            termYearCode = scheduleSettings.term.get().dropLastWhile { it != '-' }.dropLast(1),
            termId = scheduleSettings.term.get().takeLastWhile { it!='-' }
        ).body() ?: throw IOException("getClassroomData error")

        return responseBody.datas.cxkxjasqk.rows
    }

    override fun getCurrentCampus(): Flow<String> = freeClassroomSettings.currentCampus.flow
}