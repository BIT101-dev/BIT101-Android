package cn.bit101.api.service.bit101

import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetMessagesDataModel
import cn.bit101.api.model.http.bit101.GetMessagesNumberDataModel
import cn.bit101.api.model.http.bit101.GetSeparateMessagesNumberDataModel
import cn.bit101.api.model.http.bit101.PostSystemMessageReadDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface MessageApiService : ApiService {
    @GET("/messages/unread_num")
    suspend fun getMessagesNumber(): Response<GetMessagesNumberDataModel.Response>

    @GET("/messages/unread_nums")
    suspend fun getSeparateMessagesNumber(): Response<GetSeparateMessagesNumberDataModel.Response>

    @GET("/messages")
    suspend fun getMessages(
        @Query("last_id") lastID: Int? = null,
        @Query("type") type: String
    ) : Response<GetMessagesDataModel.Response>

    @POST("/messages/system")
    suspend fun sendSystemMessage(
        @Body body: PostSystemMessageReadDataModel.Body
    ) : Response<Void>
}