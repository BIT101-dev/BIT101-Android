package cn.bit101.android.status.base

interface LoginStatusManager {
    suspend fun checkLogin(): Boolean
    suspend fun login(username: String, password: String): Boolean
    suspend fun logout()
}