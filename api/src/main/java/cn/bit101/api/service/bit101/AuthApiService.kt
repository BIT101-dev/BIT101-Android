package cn.bit101.api.service.bit101

import cn.bit101.api.model.http.bit101.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("user/webvpn_verify_init")
    suspend fun webVpnVerifyInit(
        @Body body: PostWebvpnVerifyInitDataModel.Body
    ): Response<PostWebvpnVerifyInitDataModel.Response>

    @POST("/user/webvpn_verify")
    suspend fun webVpnVerify(
        @Body body: PostWebvpnVerifyDataModel.Body
    ): Response<PostWebvpnVerifyDataModel.Response>

    @POST("/user/mail_verify")
    suspend fun mailVerify(
        @Body body: PostMailVerifyDataModel.Body
    ): Response<PostMailVerifyDataModel.Response>

    @POST("/user/register")
    suspend fun register(
        @Body body: PostRegisterDataModel.Body
    ): Response<PostRegisterDataModel.Response>


    @POST("/user/login")
    suspend fun login(@Body body: PostLoginDataModel.Body): Response<PostLoginDataModel.Response>
}
