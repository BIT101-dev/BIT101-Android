package cn.bit101.android.features.schedule.classroom

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.config.setting.base.TimeTable
import cn.bit101.android.data.repo.base.FreeClassroomRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.ClassroomInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
internal class FreeClassroomSearchViewModel @Inject constructor(
    private val freeClassroomRepo: FreeClassroomRepo,
    private val loginRepo: SchoolLoginRepo,
    private val settingData: FreeClassroomSettings,
    private val scheduleSettings: CourseScheduleSettings,
) : ViewModel() {
    val nowCampusFlow = freeClassroomRepo.getCurrentCampusCode()
    val nowCampusNameFlow = freeClassroomRepo.getCurrentCampusName()

    val hideBusyClassroomFlow = settingData.hideBusyClassroom.flow

    val freeMinutesThresholdFlow = settingData.freeMinutesThreshold.flow

    val getBuildingTypeStatusLiveData = MutableLiveData<SimpleDataState<List<BuildingInfo>>?>(null)

    val selectedIndices = mutableStateListOf<Int>()

    data class ClassroomBusyData(
        val classroom: ClassroomInfo,
        val prettyFreeTimes: String,
        val nextBusyTime: LocalTime,
        val nextFreeTime: LocalTime? = null,    // 只在 busy 时有值, 记录还有多久才会迎来下一个 (长于阈值的) 空闲时间
    )

    private suspend fun getBuildingTypesWithoutState(): List<BuildingInfo> {
        val nowCampusCode = nowCampusFlow.first()

        return if (nowCampusCode.isEmpty())
            freeClassroomRepo.getBuildingInfos()
        else
            freeClassroomRepo.getBuildingInfos(nowCampusCode)
    }

    private var buildingLoad: Job? = null

    fun loadBuildingTypes(): Job {
        buildingLoad?.cancel()
        getBuildingTypeStatusLiveData.value = SimpleDataState.Loading()
        return viewModelScope.launch {
            try {
                val buildings = loginRepo.withSchoolSession {
                    withContext(Dispatchers.IO) { getBuildingTypesWithoutState() }
                }
                getBuildingTypeStatusLiveData.value = SimpleDataState.Success(buildings)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                getBuildingTypeStatusLiveData.value = SimpleDataState.Fail()
            }
        }.also { buildingLoad = it }
    }

    val getClassroomsStatesMap = mutableStateMapOf<String,SimpleState>()
    val getClassroomLastStatusLiveData = mutableStateOf<SimpleState?>(null)

    internal data class ClassroomDataCache(
        val data: List<ClassroomBusyData>,
        val cacheValidUntil: LocalDateTime
    )

    val classroomDataMap = mutableStateMapOf<String, List<ClassroomBusyData>>()
    private var classroomDataCache = hashMapOf<String, ClassroomDataCache>()

    private suspend fun prepareClassroomInfos(buildingId: String): ClassroomDataCache {
        val cached = classroomDataCache[buildingId]
        val now = LocalDateTime.now()
        if (cached != null && cached.cacheValidUntil >= now) return cached
        val classrooms = if (cached != null && cached.cacheValidUntil.toLocalDate() == now.toLocalDate()) {
            cached.data.map { it.classroom }
        } else {
            freeClassroomRepo.getClassroomInfos(buildingId)
        }
        val timeTable = scheduleSettings.timeTable.get()
        require(timeTable.isNotEmpty()) { "课程节次时间表为空" }
        val threshold = settingData.freeMinutesThreshold.get()
        val currentTime = now.toLocalTime()
        val data = classrooms.map { calculateAvailability(it, timeTable, currentTime, threshold) }
            .sortedWith(compareByDescending<ClassroomBusyData> {
                if (it.nextBusyTime == LocalTime.MIN) -it.nextFreeTime!!.toSecondOfDay()
                else it.nextBusyTime.toSecondOfDay()
            }.thenBy { it.classroom.classroomName })
        val nextChange = timeTable.flatMap { listOf(it.startTime, it.endTime) }
            .filter { it > currentTime }.minOrNull() ?: LocalTime.MAX
        return ClassroomDataCache(data, nextChange.atDate(now.toLocalDate()))
    }

    internal fun calculateAvailability(
        classroom: ClassroomInfo,
        timeTable: TimeTable,
        now: LocalTime,
        freeMinutesThreshold: Long,
    ): ClassroomBusyData {
        require(timeTable.isNotEmpty()) { "课程节次时间表为空" }
        val busy = classroom.busyTimeStr.orEmpty().split(',').filter { it.isNotBlank() }
            .map { it.trim().toInt() }.distinct().sorted()
        require(busy.all { it in 1..timeTable.size }) { "教室占用节次超出时间表范围" }
        val remaining = busy.map { timeTable[it - 1] }.filter { it.endTime > now }
        val current = remaining.firstOrNull()?.takeIf { it.startTime <= now }
        var nextFree = current?.endTime
        if (nextFree != null) {
            for (next in remaining.drop(1)) {
                val gap = next.startTime.toSecondOfDay() - nextFree!!.toSecondOfDay()
                if (gap > 0 && gap >= freeMinutesThreshold.coerceAtLeast(0) * 60) break
                nextFree = maxOf(nextFree, next.endTime)
            }
        }
        val free = (1..timeTable.size).filter { it !in busy }
        val ranges = mutableListOf<IntRange>()
        for (section in free) {
            val previous = ranges.lastOrNull()
            if (previous != null && previous.last + 1 == section) ranges[ranges.lastIndex] = previous.first..section
            else ranges += section..section
        }
        val prettyFree = ranges.joinToString(", ") {
            if (it.first == it.last) "${it.first}" else "${it.first}~${it.last}"
        }.ifEmpty { "无" }
        return ClassroomBusyData(
            classroom = classroom,
            prettyFreeTimes = prettyFree,
            nextBusyTime = if (current != null) LocalTime.MIN else remaining.firstOrNull()?.startTime ?: LocalTime.MAX,
            nextFreeTime = nextFree,
        )
    }

    // 获取对应教学楼的全部教室及其空闲情况
    fun loadClassroomInfos(buildingId: String): Job? {
        if (getClassroomsStatesMap[buildingId] == SimpleState.Loading) return null
        getClassroomsStatesMap[buildingId] = SimpleState.Loading
        getClassroomLastStatusLiveData.value = SimpleState.Loading
        return viewModelScope.launch {
            try {
                val result = loginRepo.withSchoolSession { prepareClassroomInfos(buildingId) }
                classroomDataCache[buildingId] = result
                classroomDataMap[buildingId] = result.data
                getClassroomsStatesMap[buildingId] = SimpleState.Success
                getClassroomLastStatusLiveData.value = SimpleState.Success
            } catch (e: CancellationException) {
                getClassroomsStatesMap.remove(buildingId)
                getClassroomLastStatusLiveData.value = null
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                getClassroomsStatesMap[buildingId] = SimpleState.Fail
                getClassroomLastStatusLiveData.value = SimpleState.Fail
            }
        }
    }
    fun refreshAllClassroomInfo() {
        classroomDataCache.forEach { classroomDataCache[it.key] = it.value.copy(cacheValidUntil = LocalDateTime.now()) }
        val tempKeys = classroomDataCache.map { it.key }
        tempKeys.forEach {loadClassroomInfos(it)}
    }
    fun refreshClassroomInfoIfInvalid(buildingId:String) {
        if (getClassroomsStatesMap[buildingId] == SimpleState.Fail) return
        val cacheValidUntil = classroomDataCache[buildingId]?.cacheValidUntil
        if(cacheValidUntil == null || cacheValidUntil < LocalDateTime.now())
            loadClassroomInfos(buildingId)
    }

    fun isFreeNow(classroomBusyData: ClassroomBusyData,
                  nowTime: LocalTime,
                  freeMinutesThreshold: Long) : Boolean {
        return classroomBusyData.nextBusyTime == LocalTime.MAX
                || classroomBusyData.nextBusyTime.toSecondOfDay() >= nowTime.toSecondOfDay() + freeMinutesThreshold * 60
    }

    fun switchSelectState(index:Int){
        if(selectedIndices.contains(index))
            selectedIndices.remove(index)
        else
            selectedIndices.add(index)
    }
    fun clearSelectState() {
        selectedIndices.clear()
    }
}