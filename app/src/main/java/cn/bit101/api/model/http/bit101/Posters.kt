package cn.bit101.api.model.http.bit101

import cn.bit101.api.model.common.Claim
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User

class GetPostersDataModel private constructor() {
    data class ResponseItem(
        val anonymous: Boolean,
        val claim: Claim,
        val commentNum: Long,
        val createTime: String,
        val editTime: String,
        val id: Long,
        val images: List<Image>,
        val likeNum: Long,
        val public: Boolean,
        val tags: List<String>,
        val text: String,
        val title: String,
        val updateTime: String,
        val user: User
    )

    class Response : ArrayList<ResponseItem>()
}

class GetPosterDataModel private constructor() {
    data class Response(
        val anonymous: Boolean,
        val claim: Claim,
        val commentNum: Int,
        val createTime: String,
        val editTime: String,
        val id: Int,
        val images: List<Image>,
        val like: Boolean,
        val likeNum: Int,
        val own: Boolean,
        val plugins: String,
        val public: Boolean,
        val tags: List<String>,
        val text: String,
        val title: String,
        val updateTime: String,
        val user: User
    )
}


fun GetPosterDataModel.Response.toGetPostersDataModelResponseItem() = GetPostersDataModel.ResponseItem(
    anonymous = anonymous,
    claim = claim,
    commentNum = commentNum.toLong(),
    createTime = createTime,
    editTime = editTime,
    id = id.toLong(),
    images = images,
    likeNum = likeNum.toLong(),
    public = public,
    tags = tags,
    text = text,
    title = title,
    updateTime = updateTime,
    user = user
)

class PostPostersDataModel private constructor() {
    data class Body(
        val anonymous: Boolean,
        val claimId: Int,
        val imageMids: List<String>,
        val plugins: String,
        val public: Boolean,
        val tags: List<String>,
        val text: String,
        val title: String
    )

    data class Response(
        val id: Int
    )
}

class PutPosterDataModel private constructor() {
    data class Body(
        val anonymous: Boolean,
        val claimId: Int,
        val imageMids: List<String>,
        val plugins: String,
        val public: Boolean,
        val tags: List<String>,
        val text: String,
        val title: String
    )
}

class GetClaimDataModel private constructor() {
    class Response : ArrayList<Claim>()
}