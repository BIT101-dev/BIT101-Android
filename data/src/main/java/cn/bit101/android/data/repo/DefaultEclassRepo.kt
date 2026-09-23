package cn.bit101.android.data.repo

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.eclass.EclassDdlItem
import cn.bit101.android.data.eclass.EclassDdlLogic
import cn.bit101.android.data.net.WebViewCookieSync
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.EclassRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [EclassRepo] 的实现：课程列表 → 逐课程拉动态 → 纯逻辑映射成 DDL。
 *
 * 依赖注入用的是 `APIManager`（data 层拿 `Bit101Api` 的标准入口），
 * eclass 的 Retrofit 挂在带 cookieJar 的 `schoolClient` 上（见 `Bit101ApiFactory`）。
 */
@Singleton
internal class DefaultEclassRepo @Inject constructor(
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus,
) : EclassRepo {

    private companion object {
        const val ECLASS_BASE = "https://zy-eclass.bit.edu.cn"
    }

    /**
     * 会话是否可用。
     *
     * ⚠️ **失败不等于未登录**：这是一次真实网络请求，超时 / 服务器 5xx /
     * 校园网抖动都会让它失败 —— 只试一次的话，「检查登录」会把瞬时网络故障
     * 误报成「未登录」（且调用方往往把结果缓存住，用户只能重启恢复）。
     * 所以这里：① 先把 WebView 的 cookie 同步过来（登录刚完成时 OkHttp 的
     * cookie jar 里可能还是旧会话，与 [fetchHomework] 的取数路径保持一致）；
     * ② 失败再试一次，两次都失败才报告未登录。
     */
    override suspend fun isSessionAlive(): Boolean {
        syncCookies()
        repeat(2) {
            val ok = runCatching { apiManager.api.eclass.getVisitedCourses() }
                .map { it.isSuccessful }
                .getOrDefault(false)
            if (ok) return true
        }
        return false
    }

    override suspend fun fetchHomework(now: LocalDateTime): List<EclassDdlItem> {
        syncCookies()

        val courses = runCatching { apiManager.api.eclass.getVisitedCourses() }
            .getOrNull()
            ?.takeIf { it.isSuccessful }
            ?.body()
            ?.visitedCourses
            .orEmpty()

        if (courses.isEmpty()) return emptyList()

        // 逐课程并发拉取：课程之间互不依赖，串行会让同步时间随课程数线性增长；
        // 但每个分支各自 try/catch —— 一门课失败只丢它自己，不影响其它课。
        return coroutineScope {
            courses
                .map { course ->
                    async {
                        runCatching {
                            val resp = apiManager.api.eclass.getActivities(course.id)
                            if (!resp.isSuccessful) return@runCatching emptyList()
                            EclassDdlLogic.toDdlItems(
                                activities = resp.body()?.activities.orEmpty(),
                                courseName = course.name,
                                now = now,
                            )
                        }.getOrDefault(emptyList())
                    }
                }
                .awaitAll()
                .flatten()
                // 合并后再整体排序：单门课内部已排过，但跨课程需要重新排一次
                .sortedBy { it.time }
        }
    }

    override fun syncCookies() {
        WebViewCookieSync.sync(loginStatus.cookieManager, listOf(ECLASS_BASE))
    }
}
