package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.CoursesRepo
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** 只在提交状态时持锁，网络请求互不阻塞；退出或切号后拒绝旧请求写回。 */
@Singleton
internal class AccountSession @Inject constructor(
    private val status: LoginStatus,
    private val courses: CoursesRepo,
) {
    data class Identity(val sid: String, val generation: Long)
    private val mutex = Mutex()
    private var generation = 0L

    suspend fun identity() = mutex.withLock { Identity(status.sid.get(), generation) }

    suspend fun <T> commit(expected: Identity, sid: String = expected.sid, block: suspend () -> T): T =
        mutex.withLock {
            currentCoroutineContext().ensureActive()
            if (expected != Identity(status.sid.get(), generation)) throw CancellationException("账号已切换")
            withContext(NonCancellable) {
                if (sid != expected.sid) {
                    generation++
                    clearSchoolSchedule()
                    status.clear()
                    status.sid.set(sid)
                }
                block()
            }
        }

    private suspend fun clearSchoolSchedule() {
        courses.saveSchedule(emptyList(), emptyList(), term = "")
    }

    suspend fun logout(): Unit = mutex.withLock {
        withContext(NonCancellable) {
            generation++
            status.clear()
            try {
                clearSchoolSchedule()
            } catch (e: Exception) {
                // 凭据已清除，读缓存按账号屏蔽；下次登录必须先成功清理才可切入新账号。
                Log.w("AccountSession", "退出后清理课表缓存失败", e)
            }
        }
    }
}
