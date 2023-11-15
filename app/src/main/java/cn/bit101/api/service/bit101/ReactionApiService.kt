package cn.bit101.api.service.bit101

import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.CommentsOrder
import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetCommentsDataModel
import cn.bit101.api.model.http.bit101.PostCommentDataModel
import cn.bit101.api.model.http.bit101.PostLikeDataModel
import cn.bit101.api.model.http.bit101.PostStayDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface ReactionApiService : ApiService {
    @POST("/reaction/like")
    suspend fun postLike(
        @Body body: PostLikeDataModel.Body
    ): Response<PostLikeDataModel.Response>

    @GET("/reaction/comments")
    suspend fun getComments(
        @Query("obj") obj: String,
        @Query("order") order: CommentsOrder? = null,
        @Query("page") page: Int? = null,
    ): Response<GetCommentsDataModel.Response>

    @POST("/reaction/comments")
    suspend fun postComment(
        @Body body: PostCommentDataModel.Body
    ): Response<Comment>

    @DELETE("/reaction/comments/{id}")
    suspend fun deleteComment(
        @Path("id") id: String,
    ): Response<Unit>

    @POST("/reaction/stay")
    suspend fun postStay(
        @Body body: PostStayDataModel.Body
    ): Response<Unit>
}