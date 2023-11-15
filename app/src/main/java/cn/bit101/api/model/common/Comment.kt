package cn.bit101.api.model.common

data class Comment (
    val id: Int,
    val obj: String,

    // 配图
    val images: ArrayList<Image>,

    // 发表用户
    val user: User,

    val anonymous: Boolean,
    val createTime: String,
    val updateTime: String,
    val like: Boolean,
    val likeNum: Int,

    // 子评论数量
    val commentNum: Int,

    val own: Boolean,

    // 评分
    val rate: Int,

    // 回复的用户
    val replyUser: User,

    // 回复的对象
    val replyObj: String,

    // 评论内容
    val text: String,

    val sub: ArrayList<Comment>,
)