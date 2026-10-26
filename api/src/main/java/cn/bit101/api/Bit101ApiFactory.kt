package cn.bit101.api

import cn.bit101.api.converter.ConverterFactory
import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.option.ApiOption
import cn.bit101.api.option.DEFAULT_API_OPTION
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.HttpUrl
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object Bit101ApiFactory {
    // 认证不经过业务 401 拦截器，也不占用其 Dispatcher，避免并发 401 等待续期时死锁。
    private val authClient = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build()
    private val gson = GsonBuilder()
        .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    fun create(
        option: ApiOption = DEFAULT_API_OPTION,
        logger: Logger = emptyLogger,
    ): Bit101Api {

        val urls = if(option.webVpn) option.webVpnUrls
        else option.localUrls
        val backendUrl = requireNotNull(HttpUrl.parse(urls.bit101Url))
        val clientHeaders = Interceptor { chain ->
            val request = chain.request()
            val sameOrigin = request.url().scheme() == backendUrl.scheme() &&
                request.url().host() == backendUrl.host() && request.url().port() == backendUrl.port()
            val builder = request.newBuilder()
            option.bit101Headers.forEach { (name, value) ->
                if (sameOrigin) builder.header(name, value) else builder.removeHeader(name)
            }
            chain.proceed(builder.build())
        }

        val converterFactory = ConverterFactory(
            stringConverterFactory = ScalarsConverterFactory.create(),
            gsonConverterFactory = GsonConverterFactory.create(gson),
        )

        return Bit101Api(
            authRetrofit = Retrofit.Builder()
                .client(authClient.newBuilder().addNetworkInterceptor(clientHeaders).build())
                .addConverterFactory(converterFactory)
                .baseUrl(urls.bit101Url)
                .build(),
            bit101Retrofit = Retrofit.Builder()
                .client(option.bit101Client.newBuilder().addNetworkInterceptor(clientHeaders).build())
                .addConverterFactory(converterFactory)
                .baseUrl(urls.bit101Url)
                .build(),

            appRetrofit = Retrofit.Builder()
                .addConverterFactory(converterFactory)
                .baseUrl(urls.androidUrl)
                .build(),

            jwmsRetrofit = Retrofit.Builder()
                .client(option.schoolClient)
                .addConverterFactory(converterFactory)
                .baseUrl(urls.jwmsUrl)
                .build(),

            jwcRetrofit = Retrofit.Builder()
                .client(option.schoolClient)
                .addConverterFactory(converterFactory)
                .baseUrl(urls.jwcUrl)
                .build(),

            logger = logger,
        )
    }
}
