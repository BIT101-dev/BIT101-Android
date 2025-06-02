package cn.bit101.api.service.bit101

import cn.bit101.api.model.common.Image
import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.PostUploadImageByUrlDataModel
import cn.bit101.api.service.ApiService
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UploadApiService : ApiService {
    @Multipart
    @POST("/upload/image")
    suspend fun upload(
        @Part file: MultipartBody.Part,
    ): Response<Image>

    @POST("/upload/image/url")
    suspend fun uploadByUrl(
        @Body body: PostUploadImageByUrlDataModel.Body
    ): Response<Image>
}