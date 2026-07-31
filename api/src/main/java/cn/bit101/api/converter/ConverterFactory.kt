package cn.bit101.api.converter

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class ConverterFactory(
    private val stringConverterFactory: Converter.Factory,
    private val gsonConverterFactory: Converter.Factory,
) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return if(type == String::class.java) stringConverterFactory.responseBodyConverter(type, annotations, retrofit)
        else gsonConverterFactory.responseBodyConverter(type, annotations, retrofit)
    }

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return if(type == String::class.java) stringConverterFactory.stringConverter(type, annotations, retrofit)
        else gsonConverterFactory.stringConverter(type, annotations, retrofit)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return if(type == String::class.java) stringConverterFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        else gsonConverterFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }
}
