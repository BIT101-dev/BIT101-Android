package cn.bit101.android.ui.gallery.poster

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.repo.base.ReactionRepo
import cn.bit101.android.repo.base.UploadRepo
import cn.bit101.android.ui.gallery.common.ImageData
import cn.bit101.android.ui.gallery.common.SimpleState
import cn.bit101.android.ui.gallery.common.RefreshAndLoadMoreStatesCombined
import cn.bit101.android.ui.gallery.common.UploadImageData
import cn.bit101.android.ui.gallery.common.UploadImageState
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.http.bit101.GetPosterDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

/**
 * 获取帖子的状态，成功后返回帖子数据
 */
sealed interface GetPosterState {
    object Loading : GetPosterState
    object Fail : GetPosterState
    data class Success(
        val poster: GetPosterDataModel.Response
    ) : GetPosterState
}

/**
 * 编辑的评论数据
 */
data class CommentEditData(
    val text: String,
    val uploadImageData: UploadImageData,
    val anonymous: Boolean,
)

sealed interface SendCommentState {
    object Sending : SendCommentState
    object Fail : SendCommentState
    object Success : SendCommentState
}

@HiltViewModel
class PosterViewModel @Inject constructor(
    private val posterRepo: PosterRepo,
    private val reactionRepo: ReactionRepo,
    private val uploadRepo: UploadRepo,
) : ViewModel() {
    private val _getPosterStateFlow = MutableStateFlow<GetPosterState?>(null)
    val getPosterStateFlow = _getPosterStateFlow.asStateFlow()

    val commentState = RefreshAndLoadMoreStatesCombined<Comment>(viewModelScope)

    val subCommentState = RefreshAndLoadMoreStatesCombined<Comment>(viewModelScope)

    private val _commentEditDataFlow = MutableStateFlow<CommentEditData?>(null)
    val commentEditDataFlow = _commentEditDataFlow.asStateFlow()

    private val _commentForCommentEditDataMapFlow = MutableStateFlow<Map<Pair<Int, Int>, CommentEditData?>>(emptyMap())
    val commentForCommentEditDataMapFlow = _commentForCommentEditDataMapFlow.asStateFlow()

    private val _showMoreStateFlow = MutableStateFlow<Pair<Boolean, Long?>>(Pair(false, null))
    val showMoreStateFlow = _showMoreStateFlow.asStateFlow()

    private val _commentLikeStatesFlow = MutableStateFlow<Set<Long>>(emptySet())
    val commentLikeStatesFlow = _commentLikeStatesFlow.asStateFlow()

    private val _posterLikeStatesFlow = MutableStateFlow<Set<Long>>(emptySet())
    val posterLikeStatesFlow = _posterLikeStatesFlow.asStateFlow()

    private val _sendCommentToPosterStateFlow = MutableStateFlow<SendCommentState?>(null)
    val sendCommentToPosterStateFlow = _sendCommentToPosterStateFlow.asStateFlow()

    private val _sendCommentToCommentStateFlow = MutableStateFlow<SendCommentState?>(null)
    val sendCommentToCommentStateFlow = _sendCommentToPosterStateFlow.asStateFlow()

    val showCommentDialogStateLiveData = MutableLiveData<Pair<Comment, Comment>>(null)


    val deletePosterStateLiveData = MutableLiveData<SimpleState>(null)
    val deleteCommentStateLiveData = MutableLiveData<SimpleState>(null)

    fun setDeletePosterState(state: SimpleState?) {
        deletePosterStateLiveData.value = state
    }

    fun setDeleteCommentState(state: SimpleState?) {
        deleteCommentStateLiveData.value = state
    }

    /**
     * 设置显示评论对话框的状态
     */
    fun setShowCommentDialogState(comment: Comment?, subComment: Comment?) {
        if(comment == null || subComment == null) showCommentDialogStateLiveData.value = null
        else showCommentDialogStateLiveData.value = Pair(comment, subComment)
    }

    /**
     * 加载子评论
     */
    fun getSubComments(id: Long) {
        subCommentState.refresh {
            posterRepo.getCommentsOfCommentById(id)
        }
    }

    /**
     * 加载更多子评论
     */
    fun loadMoreSubComments(id: Long) {
        subCommentState.loadMore { page ->
            posterRepo.getCommentsOfCommentById(id, page.toInt())
        }
    }


    /**
     * 从评论列表中找到对应的评论
     */
    private fun findCommentById(id: Long, comments: List<Comment>): Comment? {
        comments.forEach {
            if(it.id.toLong() == id) return it
            val comment = findCommentById(id, it.sub)
            if(comment != null) return comment
        }
        return null
    }

    /**
     * 从评论列表中找到对应的评论
     */
    fun findCommentById(id: Long) = findCommentById(id, commentState.dataFlow.value)

    /**
     * 设置显示更多评论的状态
     */
    fun setShowMoreState(show: Boolean, id: Long? = null) {
        val lastValue = showMoreStateFlow.value
        _showMoreStateFlow.value = if(id == null) lastValue.copy(first = show)
        else lastValue.copy(first = show, second = id)
    }

    /**
     * 获取帖子
     */
    fun getPosterById(id: Long) {
        _getPosterStateFlow.value = GetPosterState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val poster = posterRepo.getPosterById(id)
                _getPosterStateFlow.value = GetPosterState.Success(poster)
            } catch (e: Exception) {
                _getPosterStateFlow.value = GetPosterState.Fail
            }
        }
    }

    /**
     * 刷新评论
     */
    fun refreshComments(id: Long) = commentState.refresh {
        Log.i("refreshComments", "refreshComments")
        posterRepo.getCommentsById(id)
    }

    /**
     * 加载更多评论
     */
    fun loadMoreComments(id: Long) = commentState.loadMore {  page ->
        posterRepo.getCommentsById(id, page.toInt())
    }

    /**
     * 编辑对帖子的评论
     */
    fun editComment(commentEditData: CommentEditData) {
        _commentEditDataFlow.value = commentEditData
    }

    /**
     * 编辑对评论的评论
     */
    fun editCommentOfComment(
        comment: Comment,
        subComment: Comment,
        commentEditData: CommentEditData
    ) {
        _commentForCommentEditDataMapFlow.value = commentForCommentEditDataMapFlow.value.plus(Pair(Pair(comment.id, subComment.id), commentEditData))
    }

    /**
     * 对帖子点赞
     */
    fun likePoster(id: Long) {
        Log.i("like poster", id.toString())

        _posterLikeStatesFlow.value = _posterLikeStatesFlow.value.plus(id)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = reactionRepo.likePoster(id)
                _getPosterStateFlow.value = (_getPosterStateFlow.value as GetPosterState.Success).copy(
                    poster = (_getPosterStateFlow.value as GetPosterState.Success).poster.copy(
                        like = res.like,
                        likeNum = res.likeNum
                    )
                )
            } catch (_: Exception) { }
            _posterLikeStatesFlow.value = _posterLikeStatesFlow.value.minus(id)
        }
    }

    /**
     * 从评论列表中找到对应的评论，修改其点赞状态
     */
    private fun changeLike(
        _comments: List<Comment>,
        id: Long,
        like: Boolean,
        likeNum: Int,
    ): List<Comment> {
        val comments = _comments.toMutableList()
        val size = comments.size
        for(i in 0 until size) {
            if(comments[i].id.toLong() == id) {
                Log.i("comments[i]", comments[i].toString())
                comments[i] = comments[i].copy(
                    like = like,
                    likeNum = likeNum
                )
            }
            comments[i] = comments[i].copy(
                sub = ArrayList(
                    changeLike(
                        comments[i].sub,
                        id, like, likeNum
                    )
                )
            )
        }
        return comments
    }

    /**
     * 对评论点赞
     */
    fun likeComment(id: Long) {
        _commentLikeStatesFlow.value = commentLikeStatesFlow.value.plus(id)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = reactionRepo.likeComment(id)

                // 点赞成功后，更新评论列表
                commentState.setData(
                    changeLike(
                        commentState.dataFlow.value,
                        id, res.like, res.likeNum
                    )
                )

                // 子评论列表也要更新
                subCommentState.setData(
                    changeLike(
                        subCommentState.dataFlow.value,
                        id, res.like, res.likeNum,
                    )
                )


            } catch (e: Exception) {
                e.printStackTrace()
            }
            _commentLikeStatesFlow.value = commentLikeStatesFlow.value.minus(id)
        }
    }

    /**
     * 上传对帖子的评论中的图片
     */
    fun uploadImage(context: Context, uri: Uri) {

        // 先添加到列表中，再上传
        val commentEditData = commentEditDataFlow.value
        _commentEditDataFlow.value = commentEditData?.copy(
            uploadImageData = commentEditData.uploadImageData.copy(
                images = commentEditData.uploadImageData.images.plus(Pair(ImageData.Local(uri), UploadImageState.Loading))
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = uploadRepo.uploadImage(context, uri)

                // 上传成功后，更新列表
                val commentEditData2 = commentEditDataFlow.value
                _commentEditDataFlow.value = commentEditData2?.copy(
                    uploadImageData = commentEditData2.uploadImageData.copy(
                        images = commentEditData2.uploadImageData.images.map {
                            if(it.first is ImageData.Local && (it.first as ImageData.Local).uri == uri) Pair(it.first, UploadImageState.Success(res))
                            else it
                        }
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()

                // 上传失败后，更新列表
                val commentEditData2 = commentEditDataFlow.value
                _commentEditDataFlow.value = commentEditData2?.copy(
                    uploadImageData = commentEditData2.uploadImageData.copy(
                        images = commentEditData2.uploadImageData.images.map {
                            if(it.first is ImageData.Local && (it.first as ImageData.Local).uri == uri) Pair(it.first, UploadImageState.Fail)
                            else it
                        }
                    )
                )
            }
        }
    }

    /**
     * 上传对评论的评论中的图片
     */
    fun uploadImageForComment(
        context: Context,
        comment: Comment,
        subComment: Comment,
        uri: Uri
    ) {

        // 先添加到列表中，再上传
        val commentEditDataMap = commentForCommentEditDataMapFlow.value
        val commentEditData = commentEditDataMap[Pair(comment.id, subComment.id)] ?: CommentEditData(
            text = "",
            uploadImageData = UploadImageData(
                ifUpdate = false,
                images = emptyList()
            ),
            anonymous = false,
        )
        _commentForCommentEditDataMapFlow.value = commentEditDataMap.plus(Pair(
            Pair(comment.id, subComment.id), commentEditData.copy(
            uploadImageData = commentEditData.uploadImageData.copy(
                images = commentEditData.uploadImageData.images.plus(Pair(ImageData.Local(uri), UploadImageState.Loading))
            )
        )))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = uploadRepo.uploadImage(context, uri)
                // 上传成功后，更新列表

                val commentEditDataMap2 = commentForCommentEditDataMapFlow.value
                val commentEditData2 = commentEditDataMap2[Pair(comment.id, subComment.id)] ?: CommentEditData(
                    text = "",
                    uploadImageData = UploadImageData(
                        ifUpdate = false,
                        images = emptyList()
                    ),
                    anonymous = false,
                )

                _commentForCommentEditDataMapFlow.value = commentEditDataMap2.plus(Pair(
                    Pair(comment.id, subComment.id), commentEditData2.copy(
                    uploadImageData = commentEditData2.uploadImageData.copy(
                        images = commentEditData2.uploadImageData.images.map {
                            if(it.first is ImageData.Local && (it.first as ImageData.Local).uri == uri) Pair(it.first, UploadImageState.Success(res))
                            else it
                        }
                    )
                )))

            } catch (e: Exception) {
                e.printStackTrace()

                // 上传失败后，更新列表
                val commentEditDataMap2 = commentForCommentEditDataMapFlow.value
                val commentEditData2 = commentEditDataMap2[Pair(comment.id, subComment.id)] ?: CommentEditData(
                    text = "",
                    uploadImageData = UploadImageData(
                        ifUpdate = false,
                        images = emptyList()
                    ),
                    anonymous = false,
                )

                _commentForCommentEditDataMapFlow.value = commentEditDataMap2.plus(Pair(
                    Pair(comment.id, subComment.id), commentEditData2.copy(
                    uploadImageData = commentEditData2.uploadImageData.copy(
                        images = commentEditData2.uploadImageData.images.map {
                            if(it.first is ImageData.Local && (it.first as ImageData.Local).uri == uri) Pair(it.first, UploadImageState.Fail)
                            else it
                        }
                    )
                )))
            }
        }
    }


    /**
     * 发送对帖子的评论
     */
    fun sendCommentToPoster(
        id: Long,
        editData: CommentEditData
    ) {
        _sendCommentToPosterStateFlow.value = SendCommentState.Sending
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val comment = reactionRepo.sendCommentToPoster(
                    id = id,
                    text = editData.text,
                    replyUid = 0,
                    anonymous = editData.anonymous,
                    images = if(editData.uploadImageData.ifUpdate) editData.uploadImageData.images.mapNotNull {
                        if(it.second is UploadImageState.Success) {
                            (it.second as UploadImageState.Success).image.mid
                        } else null
                    } else emptyList()
                )
                _sendCommentToPosterStateFlow.value = SendCommentState.Success

                // 发送成功后，清空编辑框
                _commentEditDataFlow.value = CommentEditData(
                    text = "",
                    uploadImageData = UploadImageData(
                        ifUpdate = false,
                        images = emptyList()
                    ),
                    anonymous = false,
                )

                // 发送成功后，添加到评论列表的第一个
                val comments = commentState.dataFlow.value
                commentState.setData(comments.toMutableList().apply {
                    add(0, comment)
                })

            } catch (e: Exception) {
                e.printStackTrace()
                _sendCommentToPosterStateFlow.value = SendCommentState.Fail

            }
        }
    }

    /**
     * 添加对评论的评论
     */
    private fun addCommentToComment(
        _comments: List<Comment>,
        id: Int,
        comment: Comment,
        deep: Int = 1,
    ): List<Comment> {
        if(deep > 3) return _comments

        val comments = _comments.toMutableList()
        val size = comments.size
        for(i in 0 until size) {
            if(comments[i].id == id) {
                comments[i] = comments[i].copy(
                    sub = ArrayList(
                        comments[i].sub.toMutableList().apply {
                            add(0, comment)
                        }
                    ),
                    commentNum = comments[i].commentNum + 1,
                )
            }
            comments[i] = comments[i].copy(
                sub = ArrayList(
                    addCommentToComment(
                        comments[i].sub,
                        id, comment,
                        deep = deep + 1
                    )
                )
            )
        }
        return comments
    }

    /**
     * 发送对评论的评论
     */
    fun sendCommentToComment(
        comment: Comment,
        subComment: Comment,
        editData: CommentEditData,
    ) {
        _sendCommentToCommentStateFlow.value = SendCommentState.Sending
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resComment = reactionRepo.sendCommentToComment(
                    id = comment.id.toLong(),
                    text = editData.text,
                    replyComment = subComment,
                    anonymous = editData.anonymous,
                    images = if (editData.uploadImageData.ifUpdate) editData.uploadImageData.images.mapNotNull {
                        if (it.second is UploadImageState.Success) {
                            (it.second as UploadImageState.Success).image.mid
                        } else null
                    } else emptyList()
                )
                _sendCommentToCommentStateFlow.value = SendCommentState.Success

                // 发送成功后，清空编辑框
                val commentEditDataMap = commentForCommentEditDataMapFlow.value
                _commentForCommentEditDataMapFlow.value = commentEditDataMap.minus(Pair(comment.id, subComment.id))

                // 发送成功后，将评论添加到对应的评论列表中
                // 子评论的评论列表
                val subComments = subCommentState.dataFlow.value.toMutableList()
                subCommentState.setData(subComments.apply {
                    add(0, resComment)
                })

                // 帖子评论的评论列表
                val allComments = commentState.dataFlow.value
                commentState.setData(addCommentToComment(
                    allComments, comment.id, resComment
                ))

                // 关闭评论对话框
                setShowCommentDialogState(null, null)
            } catch (e: Exception) {
                e.printStackTrace()

                _sendCommentToCommentStateFlow.value = SendCommentState.Fail
            }
        }
    }


    fun deletePosterById(id: Long) {
        deletePosterStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                posterRepo.deletePosterById(id)
                deletePosterStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                deletePosterStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    fun deleteCommentById(toLong: Long) {
        deleteCommentStateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reactionRepo.deleteComment(toLong)
                deleteCommentStateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                deleteCommentStateLiveData.postValue(SimpleState.Fail)
            }
        }
    }

    fun deleteImageOfCommentByIndex(comment: Comment, subComment: Comment, index: Int) {
        val commentEditDataMap = commentForCommentEditDataMapFlow.value
        val commentEditData = commentEditDataMap[Pair(comment.id, subComment.id)] ?: return
        _commentForCommentEditDataMapFlow.value = commentEditDataMap.plus(Pair(
            Pair(comment.id, subComment.id), commentEditData.copy(
                uploadImageData = commentEditData.uploadImageData.copy(
                    images = commentEditData.uploadImageData.images.filterIndexed { i, _ -> i != index }
                )
            )
        ))
    }

    fun deleteImageOfPosterByIndex(index: Int) {
        val commentEditData = commentEditDataFlow.value ?: return
        _commentEditDataFlow.value = commentEditData.copy(
            uploadImageData = commentEditData.uploadImageData.copy(
                images = commentEditData.uploadImageData.images.filterIndexed { i, _ -> i != index }
            )
        )
    }
}