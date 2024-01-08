package cn.bit101.api.service.school

import cn.bit101.api.model.http.school.*
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface SchoolLexueApiService : ApiService {

    @GET("/")
    suspend fun get(): Response<GetLexueIndexDataModel.Response>

    @FormUrlEncoded
    @POST("/calendar/export.php")
    suspend fun getCalendarUrl(
        @Field("sesskey") sesskey: String,
        @Field("_qf__core_calendar_export_form") qfCoreCalendarExportForm: String = "1",
        @Field("events[exportevents]") events: String = "all",
        @Field("period[timeperiod]") period: String = "recentupcoming",
        @Field("generateurl") generateurl: String = "获取日历网址",
    ): Response<GetCalendarUrlDataModel.Response>

    @GET
    suspend fun getCalendar(
        @Url url: String,
    ): Response<GetCalendarDataModel.Response>
}