package cn.bit101.android.data.repo.base

import cn.bit101.android.data.eclass.EclassDdlItem
import java.time.LocalDateTime

/**
 * 课程中心（eclass）的取数入口。
 *
 * 职责边界：**只负责「拿到数据并映射成 DDL 条目」**，不碰数据库、不碰 UI。
 * 写入 `ddl_schedule` 由调用方（DDL 同步流程）负责 —— 与乐学源的接入方式保持一致。
 */
interface EclassRepo {

    /**
     * 会话是否仍然可用。
     *
     * 判据是「能否拉到课程列表」：接口对未登录请求会 302 回登录页，
     * Retrofit 会把它当成非 2xx，所以这里看 `isSuccessful` 就够。
     */
    suspend fun isSessionAlive(): Boolean

    /**
     * 拉取所有课程的作业，映射成 DDL 条目（按截止时间升序）。
     *
     * ⚠️ **单个课程失败不该拖垮整次同步** —— 逐课程 try/catch，
     * 一门课超时/无权限只丢它自己。这在课程多、网络不稳时很重要。
     */
    suspend fun fetchHomework(now: LocalDateTime = LocalDateTime.now()): List<EclassDdlItem>

    /**
     * 把 WebView 里登录得到的 cookie 同步到 OkHttp 使用的 cookie store。
     *
     * 必须在任何请求之前调用 —— 用户刚在 WebView 里登录完时，
     * cookie 还只在 WebView 那边（详见 `WebViewCookieSync` 的说明）。
     */
    fun syncCookies()
}
