package cn.bit101.api.converter

import cn.bit101.api.converter.lexue.GetCalendarConvertFactory
import cn.bit101.api.converter.lexue.GetCalendarUrlConvertFactory
import cn.bit101.api.converter.lexue.GetIndexConvertFactory
import cn.bit101.api.converter.login.GetSchoolInitLoginConvertFactory
import cn.bit101.api.converter.login.PostSchoolLoginConvertFactory
import cn.bit101.api.helper.Logger
import cn.bit101.api.model.http.school.GetCalendarDataModel
import cn.bit101.api.model.http.school.GetCalendarUrlDataModel
import cn.bit101.api.model.http.school.GetLexueIndexDataModel
import cn.bit101.api.model.http.school.GetSchoolInitLoginDataModel
import cn.bit101.api.model.http.school.PostSchoolLoginDataModel
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class ConverterFactory(
    private val stringConverterFactory: Converter.Factory,
    private val gsonConverterFactory: Converter.Factory,
    logger: Logger,
) : Converter.Factory() {

    private val responseBodyConverterFactoryMap = mapOf<Class<*>, Converter.Factory>(
        // 解析字符串
        Pair(String::class.java, stringConverterFactory),

        // 解析乐学主页
        Pair(GetLexueIndexDataModel.Response::class.java, GetIndexConvertFactory(logger)),

        // 获取课程表链接
        Pair(GetCalendarUrlDataModel.Response::class.java, GetCalendarUrlConvertFactory(logger)),

        // 获取课程表
        Pair(GetCalendarDataModel.Response::class.java, GetCalendarConvertFactory(logger)),

        // 初始化登录
        Pair(GetSchoolInitLoginDataModel.Response::class.java, GetSchoolInitLoginConvertFactory(logger)),

        // 登录
        Pair(PostSchoolLoginDataModel.Response::class.java, PostSchoolLoginConvertFactory(logger)),
    )

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val factory = responseBodyConverterFactoryMap[type]
        return if(factory != null) factory.responseBodyConverter(type, annotations, retrofit)
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