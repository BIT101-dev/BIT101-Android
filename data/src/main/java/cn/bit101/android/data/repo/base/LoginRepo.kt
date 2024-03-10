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
}