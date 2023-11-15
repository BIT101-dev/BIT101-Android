package cn.bit101.android.repo.base

import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.http.bit101.PostLikeDataModel

interface ReactionRepo {
    suspend fun likePoster(id: Long): PostLikeDataModel.Response

    suspend fun likeComment(id: Long): PostLikeDataModel.Response

    suspend fun sendCommentToPoster(
        id: Long,
        text: String,
        replyUid: Int,
        anonymous: Boolean = false,
        images: List<String> = emptyList(),
    ): Comment

    suspend fun sendCommentToComment(
        id: Long,
        text: String,
        replyComment: Comment,
        anonymous: Boolean = false,
        images: List<String> = emptyList(),
    ): Comment

    suspend fun deleteComment(id: Long)
}