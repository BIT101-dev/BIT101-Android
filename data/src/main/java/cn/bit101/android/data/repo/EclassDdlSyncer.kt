package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.eclass.EclassDdlLogic
import cn.bit101.android.data.repo.base.DDLScheduleRepo
import cn.bit101.android.data.repo.base.EclassRepo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 把课程中心（eclass）的作业同步进本地 DDL 表 —— **唯一实现**。
 *
 * 以前这段逻辑只写在 `DDLScheduleViewModel` 里，DDL 设置页想做
 * 「重新拉取延河课堂作业」时够不着它（页面级 VM，设置页是另一个 VM）。
 * 抽成单例后两边共用，合并规则只此一份。
 *
 * 合并规则：按 `uid` 做「不存在插入 / 存在更新」；已存在的**保留
 * `id` 与 `done`** —— 用户勾过的「已完成」不能被同步冲掉。
 */
@Singleton
class EclassDdlSyncer @Inject constructor(
    private val eclassRepo: EclassRepo,
    private val ddlScheduleRepo: DDLScheduleRepo,
) {

    /** @return 是否成功拉到数据（空列表 / 异常都算失败）。 */
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
