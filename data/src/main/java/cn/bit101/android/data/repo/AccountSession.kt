package cn.bit101.android.data.repo

import cn.bit101.android.config.user.base.LoginStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** 只在提交状态时持锁，网络请求互不阻塞；退出或切号后拒绝旧请求写回。 */
@Singleton
internal class AccountSession @Inject constructor(private val status: LoginStatus) {
    data class Identity(val sid: String, val generation: Long)
    private val mutex = Mutex()
    private var generation = 0L

    suspend fun identity() = mutex.withLock { Identity(status.sid.get(), generation) }

    suspend fun commit(expected: Identity, sid: String = expected.sid, block: suspend () -> Unit) {
        mutex.withLock {
            if (expected != Identity(status.sid.get(), generation)) throw CancellationException("账号已切换")
            withContext(NonCancellable) {
                if (sid != expected.sid) {
                    generation++
                    status.clear()
                    status.sid.set(sid)
                }
                block()
            }
        }
    }

    suspend fun logout() = mutex.withLock {
        withContext(NonCancellable) {
            generation++
            status.clear()
        }
    }
}
