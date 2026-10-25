package cn.bit101.api.service.bit101

import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetFollowersDataModel
import cn.bit101.api.model.http.bit101.GetFollowingsDataModel
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import cn.bit101.api.model.http.bit101.PostFollowDataModel
import cn.bit101.api.model.http.bit101.PutUserInfoDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface UserApiService : ApiService {
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