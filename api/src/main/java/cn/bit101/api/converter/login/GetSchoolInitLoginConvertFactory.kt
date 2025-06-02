package cn.bit101.api.converter.login

import cn.bit101.api.helper.Logger
import cn.bit101.api.model.http.school.GetSchoolInitLoginDataModel
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class GetSchoolInitLoginConvertFactory(
    logger: Logger
) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody?, GetSchoolInitLoginDataModel.Response>? {
        if(type != GetSchoolInitLoginDataModel.Response::class.java) return null

        return Converter<ResponseBody?, GetSchoolInitLoginDataModel.Response> {
            val html = it.string()
            val ifLogin = html.indexOf("用户名密码") == -1

            try {
                val doc = Jsoup.parse(html)
                val salt = doc.select("#login-croypto").first()!!.childNode(0).toString()
                val execution = doc.select("#login-page-flowkey").first()!!.childNode(0).toString()

                GetSchoolInitLoginDataModel.Response(
                    html = html,
                    salt = salt,
                    execution = execution,
                    ifLogin = ifLogin,
                )
            } catch (e: Exception) {
                GetSchoolInitLoginDataModel.Response(
                    html = html,
                    salt = null,
                    execution = null,
                    ifLogin = ifLogin,
                )
            }
        }
    }
}