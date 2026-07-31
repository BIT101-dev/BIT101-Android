package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.SmsCodeHandler

/**
 * 静默刷新登录的结果分类
 */
enum class LoginRefreshResult {
    /** 学校会话与 bit101 fakeCookie 均已刷新 */
    SUCCESS,

    /** 需要用户交互（短信验证码被取消、图形验证码），会话未刷新 */
    NEEDS_INTERACTIVE,

    /** 临时性失败（网络 I/O、429、5xx），不应登出 */
    TRANSIENT,

    /** 确定性认证失败（密码错误、账号锁定、风控拒绝、无保存凭据），应清除登录状态 */
    FAILED,

    /** 互斥锁被占用（正处于 checkLogin/login/refreshLogin 流程），无法在此刷新；调用方不应清理登录态，交由外层持锁流程处理 */
    BUSY,
}

interface LoginRepo {

    /**
     * 检查登录状态。
     *
     * 若任意一端会话失效，会用已保存的学号密码静默刷新整个会话（学校 Cookie + bit101 fakeCookie）；
     * 学校要求短信二次验证时通过共享总线弹出验证码弹窗，输入完成后继续刷新。
     * 仅确定性认证失败才清除登录状态（学号密码一并清除）。
     */
    suspend fun checkLogin(): Boolean

    /**
     * 静默刷新登录状态，使用已保存的学号密码重新建立学校会话与 bit101 fakeCookie。
     *
     * @param allowInteractive 是否允许弹窗请求短信验证码；OkHttp 拦截器等后台场景应传 false。
     */
    suspend fun refreshLogin(allowInteractive: Boolean = false): LoginRefreshResult

    /**
     * 非阻塞式静默刷新：互斥锁空闲时刷新整个会话，锁被占用时立即返回 [LoginRefreshResult.BUSY]。
     * 供 OkHttp 401 拦截器等无法承受阻塞等待锁的场景使用，避免与持锁流程重入死锁。
     */
    suspend fun tryRefreshLogin(): LoginRefreshResult

    /**
     * 登录，并保存登录状态
     * 若学校统一身份认证要求短信二次验证，将通过 [smsCodeHandler] 请求验证码
     */
    suspend fun login(
        username: String,
        password: String,
        smsCodeHandler: SmsCodeHandler? = null,
    ): Boolean

    /**
     * 登出，并清除登录状态
     */
    suspend fun logout()

    /**
     * 表明此操作需要登录状态有效才能执行 (那些 "如果获取失败可以尝试在账号设置中“检查登录状态”哦" 的都是)
     * 执行操作, 若失败则检查登录状态并重试一次
     * 检查登录状态失败或重试后仍失败则抛出异常
     * 否则返回操作返回的值
     */
    suspend fun <T> doOperationRequiresLogin(operation: suspend () -> T): T
}