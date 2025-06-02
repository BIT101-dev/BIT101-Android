package cn.bit101.api.service.school

import cn.bit101.api.model.http.school.GetCheckNeedCaptchaDataModel
import cn.bit101.api.model.http.school.GetSchoolInitLoginDataModel
import cn.bit101.api.model.http.school.PostSchoolLoginDataModel
import cn.bit101.api.service.ApiService
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SchoolUserApiService : ApiService {
    @GET("/cas/login")
    suspend fun initLogin(): Response<GetSchoolInitLoginDataModel.Response>

    @FormUrlEncoded
    @POST("/cas/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("execution") execution: String,
        @Field("croypto") salt: String,
        @Field("captcha_payload") captchaPayload: String,
        @Field("type") type: String = "UsernamePassword",
        @Field("geolocation") geolocation: String = "",
        @Field("captcha_code") captcha: String = "",
        @Field("_eventId") eventId: String = "submit",
    ): Response<PostSchoolLoginDataModel.Response>

    @GET("/cas/checkNeedCaptcha.htl")
    suspend fun checkNeedCaptcha(
        @Query("username") username: String? = null,
    ): Response<GetCheckNeedCaptchaDataModel.Response>

    @GET("/cas/getCaptcha.htl")
    suspend fun getCaptcha(): Response<String>
}