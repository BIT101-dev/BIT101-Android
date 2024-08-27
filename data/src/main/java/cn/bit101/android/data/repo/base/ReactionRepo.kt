package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.http.bit101.PostLikeDataModel

interface ReactionRepo {

    /**
     * 点赞帖子
     */
    suspend fun likePoster(id: Long): PostLikeDataModel.Response

    /**
     * 点赞评论
     */
    suspend fun likeComment(id: Long): PostLikeDataModel.Response

    /**
     * 发送对于帖子的评论
     */
    suspend fun sendCommentToPoster(
        id: Long,
        text: String,
        replyUid: Int,
        anonymous: Boolean = false,
        images: List<String> = emptyList(),
    ): Comment

    /**
     * 发送对于评论的回复
     */
    suspend fun sendCommentToComment(
        id: Long,
        text: String,
        replyComment: Comment,
        anonymous: Boolean = false,
        images: List<String> = emptyList(),
    ): Comment

    /**
     * 删除评论或回复
     */
    suspend fun deleteComment(id: Long)
}