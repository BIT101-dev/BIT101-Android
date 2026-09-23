package cn.bit101.api

import cn.bit101.api.converter.ConverterFactory
import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.option.ApiOption
import cn.bit101.api.option.DEFAULT_API_OPTION
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object Bit101ApiFactory {
    private val gson = GsonBuilder()
        .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    fun create(
        option: ApiOption = DEFAULT_API_OPTION,
        logger: Logger = emptyLogger,
    ): Bit101Api {

        val urls = if(option.webVpn) option.webVpnUrls
        else option.localUrls

        val converterFactory = ConverterFactory(
            stringConverterFactory = ScalarsConverterFactory.create(),
            gsonConverterFactory = GsonConverterFactory.create(gson),
        )

        return Bit101Api(
            bit101Retrofit = Retrofit.Builder()
                .client(option.bit101Client)
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

            // 课程中心（eclass）。同样挂 schoolClient —— 它的会话是**纯 cookie**，
            // 与学校其它站点共用同一个 cookie store，不需要额外拦截器。
            eclassRetrofit = Retrofit.Builder()
                .client(option.schoolClient)
                .addConverterFactory(converterFactory)
                .baseUrl(urls.eclassUrl)
                .build(),

            logger = logger,
        )
    }
}
