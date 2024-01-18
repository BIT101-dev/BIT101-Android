package cn.bit101.api.converter.lexue

import cn.bit101.api.model.http.school.*
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class GetIndexConvertFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody?, GetLexueIndexDataModel.Response>? {
        if(type != GetLexueIndexDataModel.Response::class.java) return null

        return Converter<ResponseBody?, GetLexueIndexDataModel.Response> {
            val html = it.string()
            val sesskey = Regex("[\"|']sesskey[\"|']:[\"|']([^\"|']+?)[\"|']").find(html)?.groupValues?.get(1)

            GetLexueIndexDataModel.Response(
                html = html,
                sesskey = sesskey,
            )
        }
    }
}