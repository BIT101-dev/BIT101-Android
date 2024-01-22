package cn.bit101.api.model.http.bit101

import cn.bit101.api.model.common.*

// 点赞，需要登录
class PostLikeDataModel private constructor() {
    data class Body(
        val obj: String,
    )

    data class Response(
        // 操作后点赞状态
        val like: Boolean,

        // 操作后点赞数
        val likeNum: Int,
    )
}

// 评论列表，需要登录
class GetCommentsDataModel private constructor() {
    class Response : ArrayList<Comment>()
}

// 评论，需要登录
class PostCommentDataModel private constructor() {
    data class Body(
        val obj: String,
        val text: String,

        val replyObj: String = "",
        // 回复用户
        val replyUid: Int? = null,

        val anonymous: Boolean? = null,
        val rate: Int? = null,
        val imageMids: ArrayList<String>,

    )
}

// 停留时间，需要登录
class PostStayDataModel private constructor() {
    data class Body(
        val obj: String,
        val time: Int,
    )
}