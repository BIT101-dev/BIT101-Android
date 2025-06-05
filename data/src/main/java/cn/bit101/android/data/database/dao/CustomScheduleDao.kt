package cn.bit101.android.data.database.dao

import androidx.room.*
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
internal interface CustomScheduleDao {
    /**
     * 获取所有自定义日程
     */
    @Query("SELECT * FROM custom_schedule")
    fun getAllSchedules(): Flow<List<CustomScheduleEntity>>

    /**
     * 获取指定日期的自定义日程
     */
    @Query("SELECT * FROM custom_schedule WHERE date = :date")
    fun getSchedulesByTermWeek(date: LocalDate): Flow<List<CustomScheduleEntity>>

    /**
     * 插入自定义日程
     */
    @Insert
    suspend fun insertSchedule(course: CustomScheduleEntity)

    /**
     * 插入若干自定义日程
     */
    @Insert
    suspend fun insertSchedules(courses: List<CustomScheduleEntity>)

    /**
     * 更新自定义日程
     */
    @Update
    suspend fun updateSchedule(course: CustomScheduleEntity)

    /**
     * 删除自定义日程
     */
    @Delete
    suspend fun deleteSchedule(course: CustomScheduleEntity)

    /**
     * 删除所有自定义日程
     */
    @Query("DELETE FROM custom_schedule")
    suspend fun deleteAllSchedules()
}