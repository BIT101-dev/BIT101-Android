package cn.bit101.api.converter.login

import android.util.Log
import cn.bit101.api.model.http.school.GetSchoolInitLoginDataModel
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class GetSchoolInitLoginConvertFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody?, GetSchoolInitLoginDataModel.Response>? {
        if(type != GetSchoolInitLoginDataModel.Response::class.java) return null

        return Converter<ResponseBody?, GetSchoolInitLoginDataModel.Response> {
            val html = it.string()
            val ifLogin = html.indexOf("帐号登录或动态码登录") == -1

            try {
                val doc = Jsoup.parse(html)
                val form = doc.select("#pwdFromId")
                val salt = form.select("#pwdEncryptSalt").attr("value")
                val execution = form.select("#execution").attr("value")

                GetSchoolInitLoginDataModel.Response(
                    html = html,
                    salt = salt,
                    execution = execution,
                    ifLogin = ifLogin,
                )
            } catch (e: Exception) {
                Log.e("PostSchoolLoginConvertFactory", e.toString())
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