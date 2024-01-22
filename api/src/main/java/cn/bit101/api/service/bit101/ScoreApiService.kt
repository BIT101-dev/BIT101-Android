package cn.bit101.api.service.bit101

import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetScoreDataModel
import cn.bit101.api.model.http.bit101.GetScoreReport
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface ScoreApiService : ApiService {
    @GET("/scores")
    suspend fun getScores(
        @Query("detail") detail: String? = null
    ): Response<GetScoreDataModel.Response>

    @GET("/scores/report")
    suspend fun getScoreReport(): Response<GetScoreReport.Response>
}