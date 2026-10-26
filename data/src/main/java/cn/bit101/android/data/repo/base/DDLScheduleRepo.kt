package cn.bit101.android.data.repo.base

import cn.bit101.android.data.database.entity.DDLScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface DDLScheduleRepo {

    /**
     * 从网络获取乐学 DDL 导出地址
     */
    val calendarUrl: Flow<String>
    suspend fun refreshSubscription()

    /** 拉取并原子更新乐学日程，保留已有完成状态。 */
    suspend fun syncCalendar()

    /**
     * 向数据库中插入 DDL
     */
    suspend fun insertDDL(ddl: DDLScheduleEntity)

    /**
     * 更新数据库中的 DDL
     */
    suspend fun updateDDL(ddl: DDLScheduleEntity)

    suspend fun setDone(ddl: DDLScheduleEntity, done: Boolean)

    /**
     * 删除数据库中的 DDL
     */
    suspend fun deleteDDL(ddl: DDLScheduleEntity)

    /**
     * 从数据库中获取特定时间之后的 DDL
     */
    fun getFutureDDL(time: LocalDateTime): Flow<List<DDLScheduleEntity>>

}
