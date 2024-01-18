package cn.bit101.api.converter.login

import cn.bit101.api.model.http.school.PostSchoolLoginDataModel
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class PostSchoolLoginConvertFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody?, PostSchoolLoginDataModel.Response>? {
        if(type != PostSchoolLoginDataModel.Response::class.java) return null

        return Converter<ResponseBody?, PostSchoolLoginDataModel.Response> {
            val html = it.string()

            val success = html.indexOf("帐号登录或动态码登录") == -1

            PostSchoolLoginDataModel.Response(
                html = html,
                success = success,
            )
        }
    }
}