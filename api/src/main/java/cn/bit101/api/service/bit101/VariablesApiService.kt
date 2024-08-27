package cn.bit101.api.service.bit101

import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetVariablesDataModel
import cn.bit101.api.model.http.bit101.PostVariablesDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface VariablesApiService : ApiService {
    @GET("/variables")
    suspend fun getVariables(
        @Query("obj") obj: String
    ): Response<GetVariablesDataModel.Response>

    @POST("/variables")
    suspend fun postVariable(
        @Body body: PostVariablesDataModel.Body
    ): Response<Void>
}