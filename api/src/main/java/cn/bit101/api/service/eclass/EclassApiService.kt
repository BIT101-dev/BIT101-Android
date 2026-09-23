package cn.bit101.api.service.eclass

import cn.bit101.api.model.http.eclass.GetEclassActivitiesDataModel
import cn.bit101.api.model.http.eclass.GetEclassCoursesDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 课程中心（eclass / 延河课堂）的服务端接口。
 *
 * ## 会话形态
 *
 * **纯 cookie**：用户在 WebView 里走统一身份认证（Keycloak）后拿到一组 cookie，
 * 之后所有 `/api/` 开头的请求只靠 cookie 鉴权，**没有额外的 token 头**。
 * 请求由带 cookieJar 的 `schoolClient` 发出（见 `Bit101ApiFactory`），
 * 而 `LoginStatus.cookieManager` 是全局共用的，所以学校域下的会话天然互通。
 *
 * ⚠️ 唯一要注意的：WebView 与 OkHttp 的 cookie 存储彼此独立，
 * 用户在 WebView 里刚登录完，必须先把 cookie 同步过来（见 `WebViewCookieSync`）。
 *
 * ## 抓到的接口（2026-09-23 实测，见 `docs/ddl-source-contract.md`）
 *
 * | 接口 | 结果 |
 * |---|---|
 * | `/api/user/recently-visited-courses` | 200 —— **当前唯一可用的「我的课程」来源** |
 * | `/api/courses/{id}/activities` | 200 —— 课程动态（资料 + 作业同表） |
 * | `/api/todos` | 200 但**恒为空**（未布置作业时无数据），故未纳入 |
 * | `/api/courses` | 403 无权限 |
 */
interface EclassApiService : ApiService {

    /**
     * 最近访问的课程。
     *
     * ⚠️ 用它当「我的课程」是**将就**：`/api/courses`（课程列表）返回 403、
     * `/api/user/courses` 与 `/api/course-list` 都不存在，逐个试过之后只剩这个可用。
     * 学生实际会点进自己所有在上的课，所以覆盖面基本够；
     * 若将来发现漏课，再找更全的入口替换（契约文档里记了这次试了哪些）。
     */
    @GET("/api/user/recently-visited-courses")
    suspend fun getVisitedCourses(): Response<GetEclassCoursesDataModel.Response>

    /**
     * 某门课程的动态。
     *
     * 资料与作业混在同一列表里 —— 判定规则见 `EclassDdlLogic`（不枚举 type，
     * 改用「作业特有字段是否存在」）。
     */
    @GET("/api/courses/{id}/activities")
    suspend fun getActivities(
        @Path("id") courseId: Int,
    ): Response<GetEclassActivitiesDataModel.Response>
}
