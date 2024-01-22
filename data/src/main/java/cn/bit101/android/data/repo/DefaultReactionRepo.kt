package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.ReactionRepo
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.http.bit101.PostCommentDataModel
import cn.bit101.api.model.http.bit101.PostLikeDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultReactionRepo @Inject constructor(
    private val apiManager: APIManager
) : ReactionRepo {

    private val api = apiManager.api

    suspend fun like(obj: String) = withContext(Dispatchers.IO) {
        val res = api.reaction.postLike(PostLikeDataModel.Body(obj))

        Log.i("DefaultReactionRepo", res.errorBody()?.string().toString())
        res.body() ?: throw Exception("like error")
    }

    override suspend fun likePoster(id: Long) = like("poster$id")
    override suspend fun likeComment(id: Long) = like("comment$id")


    private suspend fun sendComment(
        obj: String,
        text: String,
        replyObj: String = "",
        replyUid: Int = 0,
        anonymous: Boolean,
        images: List<String>
    ) = withContext(Dispatchers.IO) {
        val res = api.reaction.postComment(PostCommentDataModel.Body(
            obj = obj,
            text = text,
            replyObj = replyObj,
            replyUid = replyUid,
            anonymous = anonymous,
            imageMids = ArrayList(images),
        ))

        Log.i("sendComment", PostCommentDataModel.Body(
            obj = obj,
            text = text,
            replyUid = replyUid,
            anonymous = anonymous,
            imageMids = ArrayList(images),
        ).toString())
        Log.i("sendComment", res.body().toString())

        res.body() ?: throw Exception("send comment error")
    }

    override suspend fun sendCommentToPoster(
        id: Long,
        text: String,
        replyUid: Int,
        anonymous: Boolean,
        images: List<String>
    ) = sendComment(
        obj = "poster$id",
        text = text,
        replyUid = replyUid,
        anonymous = anonymous,
        images = images,
    )

    override suspend fun sendCommentToComment(
        id: Long,
        text: String,
        replyComment: Comment,
        anonymous: Boolean,
        images: List<String>
    ) = if(replyComment.id.toLong() != id) sendComment(
        obj = "comment$id",
        text = text,
        replyObj = "comment${replyComment.id}",
        replyUid = replyComment.user.id,
        anonymous = anonymous,
        images = images,
    ) else sendComment(
        obj = "comment$id",
        text = text,
        anonymous = anonymous,
        images = images,
    )

    override suspend fun deleteComment(id: Long) = withContext(Dispatchers.IO) {
        api.reaction.deleteComment(id.toString()).body() ?: throw Exception("delete comment error")
    }
}