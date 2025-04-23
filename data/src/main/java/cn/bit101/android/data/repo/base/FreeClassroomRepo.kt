package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.ClassroomInfo
import kotlinx.coroutines.flow.Flow

interface FreeClassroomRepo {
    suspend fun getBuildingInfos(campusId: Int? = null): List<BuildingInfo>

    suspend fun getClassroomInfos(buildingId: String): List<ClassroomInfo>

    fun getCurrentCampus() : Flow<String>
}