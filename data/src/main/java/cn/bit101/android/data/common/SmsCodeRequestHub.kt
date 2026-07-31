package cn.bit101.android.data.common

import cn.bit101.android.data.repo.PendingSmsCodeRequest
import cn.bit101.api.model.common.SmsCodeHandler
import cn.bit101.api.model.common.SmsCodeRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 共享的短信验证码请求总线。
 *
 * 无论验证码请求来自前台登录、后台自动刷新，还是 OkHttp 拦截器中的静默刷新，
 * 都通过该总线统一上抛给全局 [SmsCodeDialogHost] 弹窗完成输入，避免各层各自维护弹窗。
 */
@Singleton
class SmsCodeRequestHub @Inject constructor() {
    private val _pending = MutableStateFlow<PendingSmsCodeRequest?>(null)
    val pending: StateFlow<PendingSmsCodeRequest?> = _pending

    /**
     * 生成一个短信验证码处理器。
     *
     * 回调创建 [PendingSmsCodeRequest] 并上抛给 [pending]，随后挂起等待用户输入，
     * 输入结束后将 [pending] 复位。
     */
    fun createSmsCodeHandler(): SmsCodeHandler = SmsCodeHandler { request: SmsCodeRequest ->
        val pendingRequest = PendingSmsCodeRequest(request.maskedPhone, CompletableDeferred())
        _pending.value = pendingRequest
        try {
            pendingRequest.await()
        } finally {
            _pending.value = null
        }
    }
}
