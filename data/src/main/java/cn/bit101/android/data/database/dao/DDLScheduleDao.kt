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
interface DDLScheduleDao {
    @Query("SELECT * FROM ddl_schedule ORDER BY time ASC")
    fun getAll(): Flow<List<DDLScheduleEntity>>

    @Query("SELECT * FROM ddl_schedule WHERE time > :time ORDER BY time ASC")
    fun getFuture(time: LocalDateTime): Flow<List<DDLScheduleEntity>>

    @Query("SELECT * FROM ddl_schedule WHERE uid IN (:uid)")
    suspend fun getUIDs(uid: List<String>): List<DDLScheduleEntity>

    @Insert
    suspend fun insert(ddl: DDLScheduleEntity)

    @Update
    suspend fun update(ddl: DDLScheduleEntity)

    @Delete
    suspend fun delete(ddl: DDLScheduleEntity)

    @Query("DELETE FROM ddl_schedule")
    suspend fun deleteAll()

    @Query("DELETE FROM ddl_schedule WHERE id = :id")
    suspend fun deleteID(id: Int)
}