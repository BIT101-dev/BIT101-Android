package cn.bit101.android.data.repo.base

interface LoginRepo {

    /**
     * 检查登录状态，如果登录状态异常则清除登录状态
     */
    suspend fun checkLogin(): Boolean

    suspend fun login(username: String, password: String): Boolean

    suspend fun logout()
}