package cn.bit101.android.features.gallery.poster.utils

import android.util.Log
import cn.bit101.api.model.common.Comment
import java.util.ArrayList


/**
 * 一个访问者，用于遍历评论列表
 */
private fun List<Comment>.visit(
    visitor: (Comment) -> Comment?,
): List<Comment> {
    val newComments = ArrayList<Comment>()
    this.forEach {
        val visitedComment = visitor(it) ?: return@forEach
        newComments.add(visitedComment.copy(
            sub = ArrayList(visitedComment.sub.visit(visitor))
        ))
    }
    return newComments
}


/**
 * 从评论列表中找到对应的评论，修改其点赞状态
 */
internal fun changeLike(
    comments: List<Comment>,
    id: Long,
    like: Boolean,
    likeNum: Int,
) = comments.visit {
    if(it.id.toLong() == id) it.copy(
        like = like,
        likeNum = likeNum
    ) else it
}



/**
 * 将评论插入到对应的评论列表中，层层递归寻找对应的评论
 */
internal fun addCommentToComment(
    comments: List<Comment>,
    comment: Comment,
    subComment: Comment,
) = comments.visit {
    // 找到对应的评论，然后插入到子评论的第一个

    Log.i("CommentUtils", "addCommentToComment: ${comment.id} ${it.id}, ${comment}, $it")
    if(comment.id == it.id) it.copy(
        sub = ArrayList(it.sub.toMutableList().apply {
            add(0, subComment)
        }),
        commentNum = it.commentNum + 1,
    ) else it
}


/**
 * 删除评论
 */
internal fun deleteComment(
    comments: List<Comment>,
    id: Long,
) = comments.visit {
    if(it.id.toLong() == id) null
    else it
}