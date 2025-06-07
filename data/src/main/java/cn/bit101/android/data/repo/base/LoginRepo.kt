package cn.bit101.android.data.repo.base

interface LoginRepo {

    /**
     * 检查登录状态，如果登录状态异常则清除登录状态
     */
    suspend fun checkLogin(): Boolean

    /**
     * 登录，并保存登录状态
     */
    suspend fun login(username: String, password: String): Boolean

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