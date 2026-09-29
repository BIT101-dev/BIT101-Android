package cn.bit101.android.data.repo.base

import cn.bit101.android.data.eclass.EclassDdlItem
import java.time.LocalDateTime

/** 课程中心作业数据源。 */
interface EclassRepo {

    /** 检查登录会话。 */
    suspend fun isSessionAlive(): Boolean

    /** 拉取课程作业并按截止时间排序。 */
    suspend fun fetchHomework(now: LocalDateTime = LocalDateTime.now()): List<EclassDdlItem>

    /** 同步 WebView 登录 Cookie。 */
    fun syncCookies()
}
