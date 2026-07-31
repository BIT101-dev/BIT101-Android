package cn.bit101.android.data.repo

import kotlinx.coroutines.CompletableDeferred

// 等待用户输入的短信验证码请求
class PendingSmsCodeRequest(
    val maskedPhone: String,
    private val deferred: CompletableDeferred<String>,
) {
    fun submit(code: String) {
        deferred.complete(code)
    }

    fun cancel() {
        deferred.cancel()
    }

    suspend fun await(): String = deferred.await()
}
