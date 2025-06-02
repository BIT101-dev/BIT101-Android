package cn.bit101.api.model.common

object PapersOrder {
    const val RAND = "rand"
    const val NEW = "new"
    const val LIKE = "like"
}


object CommentsOrder {
    const val LIKE = "like"
    const val NEW = "new"
    const val OLD = "old"
    const val DEFAULT = "default"
}

object CoursesOrder {
    const val LIKE = "like"
    const val COMMENT = "comment"
    const val RATE = "rate"
    const val NEW = "new"
}

object PostersOrder {
    const val SIMILAR = "similar"
    const val LIKE = "like"
    const val NEW = "new"

    val nameAndValues
        get() = listOf(
            NameAndValue("相似", SIMILAR),
            NameAndValue("高赞", LIKE),
            NameAndValue("最新", NEW),
        )
}