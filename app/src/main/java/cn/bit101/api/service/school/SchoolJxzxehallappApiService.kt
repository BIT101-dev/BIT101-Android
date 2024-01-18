package cn.bit101.api.service.school

import cn.bit101.api.model.http.school.GetCurrentTermDataModel
import cn.bit101.api.model.http.school.PostGetScheduleDataModel
import cn.bit101.api.model.http.school.GetTermsDataModel
import cn.bit101.api.model.http.school.PostGetWeekAndDateDataModel
import cn.bit101.api.service.ApiService
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface SchoolJxzxehallappApiService : ApiService {
    @GET("/jwapp/sys/wdkbby/*default/index.do")
    suspend fun getIndex(): Response<String>

    @GET("/jwapp/sys/funauthapp/api/getAppConfig/wdkbby-{APPID}.do")
    suspend fun getAppConfig(
        @Path("APPID") appid: String = "5959167891382285",
    ): Response<String>

    @GET("/jwapp/i18n.do")
    suspend fun switchLang(
        @Query("appName") appName: String = "wdkbby",
        @Query("EMAP_LANG") emapLang: String = "zh",
    ): Response<String>

    @GET("/jwapp/sys/wdkbby/modules/jshkcb/dqxnxq.do")
    suspend fun getCurrentTerm(): Response<GetCurrentTermDataModel.Response>

    @GET("/jwapp/sys/wdkbby/modules/jshkcb/xnxqcx.do")
    suspend fun getTerms(): Response<GetTermsDataModel.Response>

    @FormUrlEncoded
    @POST("/jwapp/sys/wdkbby/modules/xskcb/cxxszhxqkb.do")
    suspend fun getSchedule(
        @Field("XNXQDM") xnxqdm: String,
    ): Response<PostGetScheduleDataModel.Response>

    @FormUrlEncoded
    @POST("/jwapp/sys/wdkbby/wdkbByController/cxzkbrq.do")
    suspend fun getWeekAndDate(
        @Field("requestParamStr") requestParamStr: String,
    ): Response<PostGetWeekAndDateDataModel.Response>
}