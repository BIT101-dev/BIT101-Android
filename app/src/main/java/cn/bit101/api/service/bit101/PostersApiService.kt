package cn.bit101.api.service.bit101

import cn.bit101.api.model.common.PostersMode
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetClaimDataModel
import cn.bit101.api.model.http.bit101.GetPosterDataModel
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.PostPostersDataModel
import cn.bit101.api.model.http.bit101.PutPosterDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface PostersApiService : ApiService {
    @GET("/posters")
    suspend fun getPosters(
        @Query("mode") mode: PostersMode? = null,
        @Query("order") order: PostersOrder? = null,
        @Query("page") page: Long? = null,
        @Query("search") search: String? = null,
        @Query("uid") uid: Int? = null,
    ): Response<GetPostersDataModel.Response>

    @GET("/posters/{id}")
    suspend fun getPosterById(
        @Path("id") id: Long,
    ): Response<GetPosterDataModel.Response>

    @POST("/posters")
    suspend fun postPoster(
        @Body body: PostPostersDataModel.Body
    ): Response<PostPostersDataModel.Response>

    @PUT("/posters/{id}")
    suspend fun putPoster(
        @Path("id") id: Int,
        @Body body: PutPosterDataModel.Body
    ): Response<Unit>

    @DELETE("/posters/{id}")
    suspend fun deletePoster(
        @Path("id") id: Int,
    ): Response<Unit>

    @GET("/posters/claims")
    suspend fun getPosterClaims(
        @Query("page") page: Long? = null,
    ): Response<GetClaimDataModel.Response>

}