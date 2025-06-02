package cn.bit101.api.service.bit101

import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetFollowersDataModel
import cn.bit101.api.model.http.bit101.GetFollowingsDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import cn.bit101.api.model.http.bit101.PostFollowDataModel
import cn.bit101.api.model.http.bit101.PostLoginDataModel
import cn.bit101.api.model.http.bit101.PostMailVerifyDataModel
import cn.bit101.api.model.http.bit101.PostRegisterDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyDataModel
import cn.bit101.api.model.http.bit101.PostWebvpnVerifyInitDataModel
import cn.bit101.api.model.http.bit101.PutUserInfoDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface UserApiService : ApiService {
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
    suspend fun login(
        @Body body: PostLoginDataModel.Body
    ): Response<PostLoginDataModel.Response>

    @GET("/user/info/{id}")
    suspend fun getUserInfo(
        @Path("id") id: String
    ): Response<GetUserInfoDataModel.Response>

    @PUT("/user/info")
    suspend fun putUserInfo(
        @Body body: PutUserInfoDataModel.Body
    ): Response<Unit>

    @POST("/user/follow/{uid}")
    suspend fun follow(
        @Path("uid") uid: Int
    ): Response<PostFollowDataModel.Response>

    @GET("/user/followings")
    suspend fun getFollowings(
        @Query("page") page: Int? = null
    ): Response<GetFollowingsDataModel.Response>

    @GET("/user/followers")
    suspend fun getFollowers(
        @Query("page") page: Int? = null
    ): Response<GetFollowersDataModel.Response>

    @GET("/user/check")
    suspend fun check(): Response<Unit>
}