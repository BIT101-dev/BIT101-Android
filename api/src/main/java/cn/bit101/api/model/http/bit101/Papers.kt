package cn.bit101.api.model.http.bit101

import cn.bit101.api.model.common.User

class GetPapersDataModel private constructor() {
    data class ResponseItem(
        val id: Int,
        val title: String,
        val intro: String,
        val likeNum: Int,
        val commentNum: Int,
        val updateTime: String,
    )

    class Response : ArrayList<ResponseItem>()
}

// 获取文章
class GetPaperByIdDataModel private constructor() {
    data class Response(
        val id: Int,
        val title: String,
        val intro: String,
        val content: String,
        val createTime: String,
        val updateTime: String,
        val updateUser: User,
        val anonymous: Boolean,
        val likeNum: Int,
        val commentNum: Int,
        val publicEdit: Boolean,
        val like: Boolean,
        val own: Boolean,
    )
}

// 新建文章，需要登录
class PostPaperDataModel private constructor() {
    data class Body(
        val title: String,
        val intro: String,
        val content: String,
        val anonymous: Boolean? = null,
        val publicEdit: Boolean? = null,
    )
    data class Response(
        val id: Int,
    )
}

// 更新文章，需要登录
class PutPaperDataModel private constructor() {
    data class Body(
        val id: Int,
        val title: String? = null,
        val intro: String? = null,
        val content: String? = null,
        val anonymous: Boolean? = null,
        val publicEdit: Boolean? = null,
    )
}