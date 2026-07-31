package cn.bit101.android.features.common.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cn.bit101.android.data.common.SmsCodeRequestHub

// 全局短信验证码弹窗宿主，观察共享总线上的待处理验证码请求并弹出输入框
@Composable
fun SmsCodeDialogHost(
    smsCodeRequestHub: SmsCodeRequestHub,
) {
    val pendingRequest by smsCodeRequestHub.pending.collectAsState()

    pendingRequest?.let { request ->
        VerificationCodeDialog(
            title = "输入短信验证码",
            message = "学校统一身份认证要求二次验证，验证码已发送至 ${request.maskedPhone}",
            onConfirm = { request.submit(it) },
            onDismiss = { request.cancel() },
        )
    }
}
