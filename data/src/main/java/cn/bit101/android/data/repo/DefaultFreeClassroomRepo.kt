package cn.bit101.android.data.repo

import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.net.SchoolCookieStore
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.FreeClassroomRepo
import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.CampusInfo
import cn.bit101.api.model.common.ClassroomInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

internal class DefaultFreeClassroomRepo @Inject constructor(
    private val apiManager: APIManager,
    private val freeClassroomSettings: FreeClassroomSettings,
    private val scheduleSettings: CourseScheduleSettings,
    private val loginStatus: LoginStatus,
): FreeClassroomRepo{
    override suspend fun getCampusInfos(): List<CampusInfo> = logApiCall(
        TAG, "获取校区列表",
        result = { "成功, 共 ${it.size} 个校区" },
    ) {
        apiManager.api.schoolJxzxehall.getCampusInfos(
            cookies = SchoolCookieStore.snapshot(loginStatus.cookieManager.cookieStore),
            webVpn = loginStatus.webVpn.get(),
        )
    }

    override suspend fun getBuildingInfos(campusCode: String?): List<BuildingInfo> = logApiCall(
        TAG, "获取教学楼列表(campusCode=$campusCode)",
        result = { "成功, 共 ${it.size} 栋教学楼" },
    ) {
        apiManager.api.schoolJxzxehall.getBuildingInfos(
            cookies = SchoolCookieStore.snapshot(loginStatus.cookieManager.cookieStore),
            webVpn = loginStatus.webVpn.get(),
            campusCode = campusCode,
        )
    }

    override suspend fun getClassroomInfos(buildingId: String): List<ClassroomInfo> {
        val api = apiManager.api

        val term = scheduleSettings.term.get().ifBlank { null }
        val firstDay = scheduleSettings.firstDay.get()
        val week = firstDay?.until(LocalDate.now(), ChronoUnit.WEEKS)?.plus(1)?.toInt()

        return logApiCall(
            TAG, "查询空教室(buildingId=$buildingId, term=$term, week=$week)",
            result = { "成功, 共 ${it.size} 间教室" },
        ) {
            api.schoolClassroom.getClassroomOccupancy(
                cookies = SchoolCookieStore.snapshot(loginStatus.cookieManager.cookieStore),
                webVpn = loginStatus.webVpn.get(),
                buildingCode = buildingId,
                semester = term,
                week = week,
            )
        }
    }

    override fun getCurrentCampusName(): Flow<String> = freeClassroomSettings.currentCampusDisplayName.flow
    override fun getCurrentCampusCode(): Flow<String> = freeClassroomSettings.currentCampusCode.flow

    private companion object {
        const val TAG = "FreeClassroomRepo"
    }
}
