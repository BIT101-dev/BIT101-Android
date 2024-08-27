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
            logger = logger,
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

            jxzxehallappRetrofit = Retrofit.Builder()
                .client(option.schoolClient)
                .addConverterFactory(converterFactory)
                .baseUrl(urls.jxzxehallappUrl)
                .build(),

            schoolLoginRetrofit = Retrofit.Builder()
                .client(option.schoolClient)
                .addConverterFactory(converterFactory)
                .baseUrl(urls.schoolLoginUrl)
                .build(),

            lexueRetrofit = Retrofit.Builder()
                .client(option.schoolClient)
                .addConverterFactory(converterFactory)
                .baseUrl(urls.lexueUrl)
                .build(),
        )
    }
}