package cn.bit101.android.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime


@Dao
internal interface DDLScheduleDao {
    /**
     * 获取所有 DDL
     */
    @Query("SELECT * FROM ddl_schedule ORDER BY time ASC")
    fun getAll(): Flow<List<DDLScheduleEntity>>

    /**
     * 获取指定时间之后的 DDL
     */
    @Query("SELECT * FROM ddl_schedule WHERE time > :time ORDER BY time ASC")
    fun getFuture(time: LocalDateTime): Flow<List<DDLScheduleEntity>>

    /**
     * 获取指定 UID 的 DDL
     */
    @Query("SELECT * FROM ddl_schedule WHERE uid IN (:uid)")
    suspend fun getUIDs(uid: List<String>): List<DDLScheduleEntity>

    /**
     * 插入 DDL
     */
    @Insert
    suspend fun insert(ddl: DDLScheduleEntity)

    /**
     * 更新 DDL
     */
    @Update
    suspend fun update(ddl: DDLScheduleEntity)

    /**
     * 删除 DDL
     */
    @Delete
    suspend fun delete(ddl: DDLScheduleEntity)

    /**
     * 删除所有 DDL
     */
    @Query("DELETE FROM ddl_schedule")
    suspend fun deleteAll()

    /**
     * 删除指定 ID 的 DDL
     */
    @Query("DELETE FROM ddl_schedule WHERE id = :id")
    suspend fun deleteID(id: Int)
}