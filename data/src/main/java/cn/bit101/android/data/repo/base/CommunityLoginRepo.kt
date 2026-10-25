package cn.bit101.android.data.repo.base

import kotlinx.coroutines.flow.Flow

data class CommunityState(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val message: String = "BIT101 尚未连接",
    val retryAt: Long = 0,
)

interface CommunityLoginRepo {
    val state: Flow<CommunityState>
    suspend fun ensureConnected(force: Boolean = false): Boolean
    suspend fun login(username: String, password: String)
    /** 用户主动统一认证；新账号自动注册，已有账号不改密码。 */
    suspend fun loginWithSchool(username: String, password: String, force: Boolean = true)
    suspend fun refresh(account: String, rejectedToken: String): String?
    /** 由前台生命周期启动，离开前台即取消。 */
    suspend fun autoLogin()
}
