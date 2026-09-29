package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.eclass.EclassDdlLogic
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.data.repo.base.EclassRepo
import javax.inject.Inject
import javax.inject.Singleton

/** 同步课程中心作业，并保留已有条目的完成状态。 */
@Singleton
class EclassDdlSyncer @Inject constructor(
    private val eclassRepo: EclassRepo,
    private val ddlScheduleRepo: DDLScheduleRepo,
) {

    /** 返回同步是否成功。 */
    suspend fun sync(): Boolean {
        return try {
            val items = eclassRepo.fetchHomework()
            if (items.isEmpty()) return false

            val existItems = HashMap<String, DDLScheduleEntity>()
            ddlScheduleRepo.getDDLByUIDs(items.map { it.uid }).forEach { existItems[it.uid] = it }

            items.forEach { item ->
                val entity = DDLScheduleEntity(
                    id = 0,
                    uid = item.uid,
                    group = EclassDdlLogic.GROUP,
                    title = item.title,
                    text = item.text,
                    time = item.time,
                    done = false,
                )
                val old = existItems[item.uid]
                if (old == null) {
                    ddlScheduleRepo.insertDDL(entity)
                } else {
                    ddlScheduleRepo.updateDDL(entity.copy(id = old.id, done = old.done))
                }
            }
            true
        } catch (e: Exception) {
            Log.e("EclassDdlSyncer", "sync eclass ddl error", e)
            false
        }
    }
}
