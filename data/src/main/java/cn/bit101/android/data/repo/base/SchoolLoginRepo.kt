package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.SmsCodeHandler

data class SchoolConnectionResult(val teachingCenterWarning: String? = null)

interface SchoolLoginRepo {
    suspend fun login(username: String, password: String, smsCodeHandler: SmsCodeHandler? = null): SchoolConnectionResult
    /** 用户主动重新验证学校账号，不访问 BIT101。 */
    suspend fun reconnect(): SchoolConnectionResult
    /** 退出当前账号，清除学校和社区凭据。 */
    suspend fun logout()
    /** 仅在明确的学校会话失效后恢复学校会话，业务最多重试一次。 */
    suspend fun <T> withSchoolSession(operation: suspend () -> T): T
}
