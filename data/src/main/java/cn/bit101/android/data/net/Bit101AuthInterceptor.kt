package cn.bit101.android.data.net

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.repo.base.CommunityLoginRepo
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Provider

internal class Bit101AuthInterceptor(
    private val loginStatus: LoginStatus,
    private val loginRepo: Provider<CommunityLoginRepo>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val sid = runBlocking { loginStatus.sid.get() }
        val token = runBlocking { loginStatus.fakeCookie.get() }
        val request = original.newBuilder().header("fake-cookie", token).build()
        val response = chain.proceed(request)
        if (response.code != 401) return response

        val retry = try {
            runBlocking {
                val updatedToken = loginRepo.get().refresh(sid, token)
                if (updatedToken == null || loginStatus.sid.get() != sid) null
                else request.newBuilder().header("fake-cookie", updatedToken).build()
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
