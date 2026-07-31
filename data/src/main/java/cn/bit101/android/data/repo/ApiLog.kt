package cn.bit101.android.data.repo

import android.util.Log
import kotlinx.coroutines.CancellationException

/**
 * 为调用 bit-login 的 API 过程添加日志, 便于调试调用流程问题
 *
 * 开始时输出步骤与参数, 结束时输出结果摘要, 失败时输出异常堆栈后原样抛出
 */
internal suspend fun <T> logApiCall(
    tag: String,
    step: String,
    result: (T) -> String = { "成功" },
    block: suspend () -> T,
): T {
    Log.i(tag, "$step 开始")
    return try {
        val r = block()
        Log.i(tag, "$step ${result(r)}")
        r
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(tag, "$step 失败: ${e.message}", e)
        throw e
    }
}
