package cn.bit101.android.data.net

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.LoginRefreshResult
import cn.bit101.android.data.repo.base.LoginRepo
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Provider

internal class Bit101AuthInterceptor(
    private val loginStatus: LoginStatus,
    private val loginRepo: Provider<LoginRepo>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("fake-cookie", runBlocking { loginStatus.fakeCookie.get() })
            .build()
        val response = chain.proceed(request)
        if (response.code != 401) return response

        val retry = try {
            runBlocking {
                // 持锁认证流程返回 BUSY，避免从拦截器重入登录锁。
                if (loginRepo.get().tryRefreshLogin() != LoginRefreshResult.SUCCESS) null
                else request.newBuilder().header("fake-cookie", loginStatus.fakeCookie.get()).build()
            }
        } catch (error: Exception) {
            response.close()
            throw error
        }
        if (retry == null) return response

        response.close()
        // proceed 不会重新进入当前拦截器；第二次 401 直接交给调用方。
        return chain.proceed(retry)
    }
}
