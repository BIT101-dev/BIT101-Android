package cn.bit101.android.data.repo

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.HashUtils
import cn.bit101.android.data.net.NetworkStatus
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.*
import cn.bit101.api.model.http.bit101.PostLoginDataModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

internal class DefaultCommunityLoginRepo @Inject constructor(
    private val apiManager: APIManager,
    private val status: LoginStatus,
    private val account: AccountSession,
    private val clock: SessionClock,
    private val network: NetworkStatus,
) : CommunityLoginRepo {
    private val mutex = Mutex()
    private val activity = MutableStateFlow(CommunityState())
    @Volatile private var attemptVersion = 0L

    private val time = flow {
        while (currentCoroutineContext().isActive) {
            emit(clock.now())
            delay(30_000)
        }
    }

    override val state: Flow<CommunityState> = combine(
        status.fakeCookie.flow, status.communityRetryAt.flow, status.communityNeedsLogin.flow, activity, time,
    ) { token, retryAt, needsLogin, active, now ->
        val connected = CommunitySessionPolicy.usable(token, now)
        active.copy(connected = connected, retryAt = retryAt, message = when {
            connected -> "BIT101 已连接"
            active.connecting -> "正在连接 BIT101"
            needsLogin -> "请手动登录 BIT101"
            retryAt > 0 -> "BIT101 暂未连接，将在前台自动重试"
            else -> "BIT101 尚未连接，不影响校园功能"
        })
    }.distinctUntilChanged()

    override suspend fun ensureConnected(force: Boolean): Boolean = try {
        connect(force = force)
    } catch (e: LoginException) {
        false
    }

    override suspend fun login(username: String, password: String) {
        require(username.isNotBlank() && password.isNotBlank()) { "请填写学号和密码" }
        if (!connect(username.trim(), HashUtils.md5(password), force = true)) {
            throw LoginException(LoginFailure.ERROR, "BIT101 登录", "BIT101 暂未连接，请稍后重试。")
        }
    }

    override suspend fun loginWithSchool(username: String, password: String, force: Boolean) {
        require(username.isNotBlank() && password.isNotBlank()) { "请填写学号和密码" }
        connect(username = username.trim(), schoolPassword = password, force = force)
    }

    override suspend fun refresh(account: String, rejectedToken: String): String? {
        if (account != this.account.identity().sid) return null
        return try {
            if (connect(rejectedToken = rejectedToken, expectedSid = account)) status.fakeCookie.get() else null
        } catch (e: LoginException) {
            null
        }
    }

    private suspend fun connect(
        username: String? = null,
        digest: String? = null,
        schoolPassword: String? = null,
        force: Boolean = false,
        rejectedToken: String? = null,
        expectedSid: String? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        val identity = account.identity()
        val observedAttempt = attemptVersion
        mutex.withLock {
            if (identity != account.identity()) throw CancellationException("账号已切换")
            if (expectedSid != null && expectedSid != identity.sid) return@withLock false
            val sid = username ?: identity.sid
            if (sid.isBlank()) return@withLock false
            if (identity.sid.isNotBlank() && sid != identity.sid) {
                throw LoginException(LoginFailure.ERROR, "BIT101 登录", "请先退出当前账号，再切换学号。")
            }
            var token = status.fakeCookie.get()
            if (digest == null && schoolPassword == null && observedAttempt != attemptVersion) return@withLock CommunitySessionPolicy.usable(token, clock.now())
            val savedSchoolPassword = status.password.get()
            val renewIdentity = schoolPassword == null && digest == null && !force && savedSchoolPassword.isNotBlank() &&
                (status.communityRenewalPending.get() || CommunitySessionPolicy.expiresSoon(token, clock.now()))
            // 在 401 清除 token 前记录到期续期，重启后仍能按统一认证的冷却时间恢复。
            if (renewIdentity && !status.communityRenewalPending.get()) {
                account.commit(identity) { status.communityRenewalPending.set(true) }
            }
            if (rejectedToken != null && token == rejectedToken && token.isNotBlank()) {
                account.commit(identity) { status.fakeCookie.set("") }
                token = ""
            }
            if (!force && CommunitySessionPolicy.usable(token, clock.now())) return@withLock true
            if (renewIdentity && (status.communityNeedsLogin.get() || clock.now() < status.communityIdentityRetryAt.get())) return@withLock false
            val identityPassword = schoolPassword ?: savedSchoolPassword.takeIf { renewIdentity }
            if (identityPassword == null && !force && (status.communityNeedsLogin.get() || clock.now() < status.communityRetryAt.get())) return@withLock false
            val passwordDigest = digest ?: status.communityPasswordDigest.get().ifBlank {
                savedSchoolPassword.takeIf { it.isNotBlank() }?.let(HashUtils::md5).orEmpty()
            }
            if (identityPassword == null && passwordDigest.isBlank()) return@withLock false
            val usesIdentity = identityPassword != null
            val identityRetryAt = clock.now() + CommunitySessionPolicy.IDENTITY_RETRY_INTERVAL
            val attempt = if (usesIdentity) (status.communityIdentityAttempts.get() + 1).coerceAtMost(4)
                else (status.communityAttempts.get() + 1).coerceAtMost(3)
            // 先预留重试间隔，前后台切换或进程退出也不会立即重复认证。
            account.commit(identity) {
                val retryAt = CommunitySessionPolicy.retryAt(clock.now(), attempt, null, usesIdentity)
                status.communityRetryAt.set(retryAt)
                if (usesIdentity) {
                    status.communityIdentityAttempts.set(attempt)
                    status.communityIdentityRetryAt.set(retryAt)
                } else status.communityAttempts.set(attempt)
            }
            activity.value = CommunityState(connecting = true)
            try {
                val auth = apiManager.api.auth
                val newToken = if (identityPassword != null) {
                    auth.loginWithSchoolIdentity(sid, identityPassword) {
                        currentCoroutineContext().ensureActive()
                        if (identity != account.identity()) throw CancellationException("账号已切换")
                    }
                } else auth.login(PostLoginDataModel.Body(sid, passwordDigest)).loginBody("BIT101 登录").fakeCookie
                if (newToken.isBlank()) throw invalidLoginResponse("BIT101 登录")
                currentCoroutineContext().ensureActive()
                account.commit(identity, sid) {
                    // 统一认证不验证已有社区密码，不能把学校密码当作已验证的社区密码保存。
                    if (!usesIdentity) status.communityPasswordDigest.set(passwordDigest)
                    status.fakeCookie.set(newToken)
                    status.communityNeedsLogin.set(false)
                    status.communityRenewalPending.set(false)
                    // 成功也保留自动统一认证的最短间隔，异常短命 token 不会形成循环。
                    status.communityIdentityRetryAt.set(
                        if (usesIdentity) identityRetryAt else minOf(status.communityIdentityRetryAt.get(), identityRetryAt)
                    )
                    status.communityIdentityAttempts.set(0)
                    status.communityAttempts.set(0)
                    status.communityRetryAt.set(0)
                }
                true
            } catch (e: Exception) {
                val failure = e.asLoginException("BIT101 登录")
                account.commit(identity) {
                    val retryAt = CommunitySessionPolicy.retryAt(clock.now(), attempt, failure.retryAfter, usesIdentity)
                    status.communityRetryAt.set(retryAt)
                    if (usesIdentity) status.communityIdentityRetryAt.set(retryAt)
                    if (renewIdentity && failure.result in listOf(LoginFailure.NEEDS_INTERACTIVE, LoginFailure.FAILED)) {
                        status.communityNeedsLogin.set(true)
                    }
                    // 后端当前将密码错误也返回 500；仅明确的 4xx 才停止自动尝试。
                    if (!usesIdentity && failure.statusCode in listOf(400, 401, 403) && !CommunitySessionPolicy.usable(token, clock.now())) {
                        status.communityNeedsLogin.set(true)
                    }
                }
                throw failure
            } finally {
                attemptVersion++
                activity.value = CommunityState()
            }
        }
    }

    override suspend fun autoLogin() {
        status.sid.flow.distinctUntilChanged().collectLatest { sid ->
            if (sid.isBlank()) return@collectLatest
            while (currentCoroutineContext().isActive) {
                if (network.isOnline()) ensureConnected()
                delay(30_000) // 仅检查本地期限；到期才发送一次登录请求。
            }
        }
    }
}
