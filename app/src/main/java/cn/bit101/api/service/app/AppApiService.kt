package cn.bit101.api.service.app

import cn.bit101.api.model.http.app.GetVersionDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.GET

interface AppApiService : ApiService {
    @GET("/version")
    suspend fun getVersion(): Response<GetVersionDataModel.Response>
}