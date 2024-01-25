package cn.bit101.api.converter.lexue

import cn.bit101.api.helper.Logger
import cn.bit101.api.model.http.school.*
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class GetCalendarUrlConvertFactory(
    logger: Logger
) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody?, GetCalendarUrlDataModel.Response>? {
        if(type != GetCalendarUrlDataModel.Response::class.java) return null

        return Converter<ResponseBody?, GetCalendarUrlDataModel.Response> {
            val html = it.string()

            val doc = Jsoup.parse(html)
            // 通过class查找日历订阅链接
            val div = doc.select(".calendarurl").text()
            val url = if (div.isEmpty()) null
            else div.substring(div.indexOf("http"))

            GetCalendarUrlDataModel.Response(
                html = html,
                url = url,
            )
        }
    }
}