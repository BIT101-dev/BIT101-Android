package cn.bit101.api.model.http.bit101

// 通过链接上传图片，需要登录
class PostUploadImageByUrlDataModel private constructor() {
    data class Body(
        // 图片链接
        val url: String,
    )
}
