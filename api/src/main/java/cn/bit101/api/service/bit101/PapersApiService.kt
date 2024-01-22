package cn.bit101.api.service.bit101

import cn.bit101.api.model.common.PapersOrder
import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetPaperByIdDataModel
import cn.bit101.api.model.http.bit101.GetPapersDataModel
import cn.bit101.api.model.http.bit101.PostPaperDataModel
import cn.bit101.api.model.http.bit101.PutPaperDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface PapersApiService : ApiService {
    @GET("/papers")
    suspend fun getPapers(
        @Query("search") search: String? = null,
        @Query("order") order: String? = null,
        @Query("page") page: Int? = null,
    ): Response<GetPapersDataModel.Response>

    @GET("/papers/{id}")
    suspend fun getPaperById(
        @Path("id") id: Int,
    ): Response<GetPaperByIdDataModel.Response>

    @POST("/papers")
    suspend fun postPaper(
        @Body body: PostPaperDataModel.Body
    ): Response<PostPaperDataModel.Response>

    @PUT("/papers/{id}")
    suspend fun putPaper(
        @Path("id") id: Int,
        @Body body: PutPaperDataModel.Body
    ): Response<Void>

    @DELETE("/papers/{id}")
    suspend fun deletePaper(
        @Path("id") id: Int,
    ): Response<Void>
}