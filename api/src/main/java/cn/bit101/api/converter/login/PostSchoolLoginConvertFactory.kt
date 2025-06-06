package cn.bit101.api.converter.login

import cn.bit101.api.helper.Logger
import cn.bit101.api.model.http.school.PostSchoolLoginDataModel
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class PostSchoolLoginConvertFactory(
    logger: Logger
) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody?, PostSchoolLoginDataModel.Response>? {
        if(type != PostSchoolLoginDataModel.Response::class.java) return null

        return Converter<ResponseBody?, PostSchoolLoginDataModel.Response> {
            val html = it.string()

            val success = html.indexOf("用户名密码") == -1

            PostSchoolLoginDataModel.Response(
                html = html,
                success = success,
            )
        }
    }
}