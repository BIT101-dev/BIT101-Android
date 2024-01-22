package cn.bit101.api.service.bit101

import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetBansDataModel
import cn.bit101.api.model.http.bit101.GetReportTypesDataModel
import cn.bit101.api.model.http.bit101.GetReportsDataModel
import cn.bit101.api.model.http.bit101.PostBanDataModel
import cn.bit101.api.model.http.bit101.PostReportDataModel
import cn.bit101.api.model.http.bit101.PutReportDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface ManageApiService : ApiService {
    @POST("/manage/reports")
    suspend fun report(
        @Body body: PostReportDataModel.Body
    ): Response<Unit>

    @GET("/manage/reports")
    suspend fun getReports(
        @Query("obj") obj: String? = null,
        @Query("page") page: Long? = null,
        @Query("status") status: Long? = null,
        @Query("uid") uid: String? = null
    ): Response<GetReportsDataModel.Response>

    @PUT("/manage/reports/{id}")
    suspend fun putReportState(
        @Path("id") id: String,
        @Body body: PutReportDataModel.Body
    ): Response<Unit>

    @GET("/manage/report_types")
    suspend fun getReportTypes(): Response<GetReportTypesDataModel.Response>

    @POST("/manage/bans")
    suspend fun ban(
        @Body body: PostBanDataModel.Body
    ): Response<Unit>

    @GET("/manage/bans")
    suspend fun getBans(
        @Query("page") page: Long? = null,
        @Query("uid") uid: String? = null
    ): Response<GetBansDataModel.Response>
}