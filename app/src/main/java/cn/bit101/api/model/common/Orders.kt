package cn.bit101.api.model.common

enum class PapersOrder {
    rand, new, like
}

enum class CommentsOrder {
    like, new, old, default,
}

enum class CoursesOrder {
    like, comment, rate, new,
}

enum class PostersOrder(val orderName: String) {
    similar("相关"),
    like("高赞"),
    new("最新")
}