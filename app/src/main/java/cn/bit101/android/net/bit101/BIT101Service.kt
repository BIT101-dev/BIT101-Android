package cn.bit101.android.net.bit101

import cn.bit101.android.database.DataStore
import cn.bit101.android.net.school.encryptPassword
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.math.BigInteger
import java.security.MessageDigest

/**
 * @author flwfdd
 * @date 2023/5/18 上午12:25
 * @description _(:з」∠)_
 */
interface BIT101Service {
    companion object {
        private const val BASE_URL = "https://bit101.flwfdd.xyz"

        val service: BIT101Service by lazy {
            val client = OkHttpClient.Builder().addInterceptor {
                // 自动添加fake-cookie
                var fakeCookie: String?
                runBlocking {
                    fakeCookie = DataStore.fakeCookieFlow.first()
                }
                val request =
                    it.request().newBuilder().addHeader("fake-cookie", fakeCookie ?: "").build()
                it.proceed(request)
            }.build()

            val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(BIT101Service::class.java)
        }
    }

    // 用户模块

    // 检查登陆状态
    @GET("/user/check")
    suspend fun check(): Response<Void>

    // 初始化统一身份认证
    @POST("/user/webvpn_verify_init")
    suspend fun webvpnVerifyInit(@Body request: WebvpnVerifyInitRequest): Response<WebvpnVerifyInitResponse>

    // 统一身份认证
    @POST("/user/webvpn_verify")
    suspend fun webvpnVerify(@Body request: WebvpnVerifyRequest): Response<WebvpnVerifyResponse>

    // 注册/重置密码/code登录
    @POST("/user/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // 获取用户信息
    @GET("/user/info")
    suspend fun userInfo(@Query("id") id: Int): Response<UserInfoResponse>
}

// 使用统一身份认证登陆BIT101
suspend fun loginBIT101(sid: String, password: String): Boolean {
    val init = BIT101Service.service.webvpnVerifyInit(WebvpnVerifyInitRequest(sid))
    if (init.body() == null) return false
    val encryptedPassword = encryptPassword(password, init.body()!!.salt) ?: return false
    val verify = BIT101Service.service.webvpnVerify(
        WebvpnVerifyRequest(
            sid,
            encryptedPassword,
            init.body()!!.execution,
            init.body()!!.cookie,
            ""
        )
    )
    if (verify.body() == null) return false
    val md5pwd = md5(password)
    val register = BIT101Service.service.register(
        RegisterRequest(
            md5pwd,
            verify.body()!!.token,
            verify.body()!!.code,
            true
        )
    )
    if (register.body() == null) return false
    DataStore.setString(DataStore.FAKE_COOKIE, register.body()!!.fake_cookie)
    return true
}

// 将字符串转为32位小写md5
fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    val messageDigest = md.digest(input.toByteArray())
    val no = BigInteger(1, messageDigest)
    var hashtext = no.toString(16)
    while (hashtext.length < 32) {
        hashtext = "0$hashtext"
    }
    return hashtext
}
