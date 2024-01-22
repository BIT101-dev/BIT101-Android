package cn.bit101.api.converter.lexue

import cn.bit101.api.helper.Logger
import cn.bit101.api.model.http.school.GetCalendarDataModel
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.property.Categories
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Uid
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.time.ZoneOffset

class GetCalendarConvertFactory(
    logger: Logger
) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody?, GetCalendarDataModel.Response>? {
        if(type != GetCalendarDataModel.Response::class.java) return null

        return Converter<ResponseBody?, GetCalendarDataModel.Response> {
            val html = it.string()

            val list = mutableListOf<GetCalendarDataModel.CalendarEvent>()
            val cal = CalendarBuilder().build(html.byteInputStream())
            cal.components.forEach { canlenderComponent ->
                val start = canlenderComponent.getProperty<DtStart>(Property.DTSTART)

                list.add(
                    GetCalendarDataModel.CalendarEvent(
                        canlenderComponent.getProperty<Uid>(Property.UID).value,
                        canlenderComponent.getProperty<Summary>(Property.SUMMARY).value,
                        canlenderComponent.getProperty<Description>(Property.DESCRIPTION).value,
                        canlenderComponent.getProperty<Categories>(Property.CATEGORIES).value,
                        start.date.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDateTime()
                    )
                )
            }

            GetCalendarDataModel.Response(
                html = html,
                calenders = list,
            )
        }
    }
}