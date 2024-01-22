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
    @GET("/authserver/login")
    suspend fun initLogin(): Response<GetSchoolInitLoginDataModel.Response>

    @FormUrlEncoded
    @POST("/authserver/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("execution") execution: String,
        @Field("captcha") captcha: String = "",
        @Field("_eventId") eventId: String = "submit",
        @Field("cllt") cllt: String = "userNameLogin",
        @Field("dllt") dllt: String = "generalLogin",
        @Field("lt") lt: String = "",
        @Field("rememberMe") rememberMe: String = "true",
    ): Response<PostSchoolLoginDataModel.Response>

    @GET("/authserver/checkNeedCaptcha.htl")
    suspend fun checkNeedCaptcha(
        @Query("username") username: String? = null,
    ): Response<GetCheckNeedCaptchaDataModel.Response>

    @GET("/authserver/getCaptcha.htl")
    suspend fun getCaptcha(): Response<String>
}