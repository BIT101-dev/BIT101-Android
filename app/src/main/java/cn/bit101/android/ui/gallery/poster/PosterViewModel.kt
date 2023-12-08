package cn.bit101.android.ui.gallery.poster

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.repo.base.ReactionRepo
import cn.bit101.android.repo.base.UploadRepo
import cn.bit101.android.ui.common.ImageData
import cn.bit101.android.ui.common.ImageDataWithUploadState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombinedOne
import cn.bit101.android.ui.common.UploadImageData
import cn.bit101.android.ui.common.UploadImageState
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.cleared
import cn.bit101.android.ui.gallery.poster.utils.addCommentToComment
import cn.bit101.android.ui.gallery.poster.utils.changeLike
import cn.bit101.android.ui.gallery.poster.utils.deleteComment
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.http.bit101.GetPosterDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
/**
 * 编辑的评论数据
 */
data class CommentEditData(
    val text: String,
    val uploadImageData: UploadImageData,
    val anonymous: Boolean,
) {
    companion object {
        fun empty() = CommentEditData(
            text = "",
            uploadImageData = UploadImageData(
                ifUpload = false,
                images = emptyList()
            ),
            anonymous = false,
        )
    }

    fun isEmpty() = text.isEmpty() && (!uploadImageData.ifUpload || uploadImageData.images.isEmpty())
}

sealed interface CommentType {
    data class ToPoster(
        val posterId: Long,
    ) : CommentType
    data class ToComment(
        val mainComment: Comment,
        val subComment: Comment,
    ) : CommentType
}

sealed interface ObjectType {
    data class PosterObject(
        val posterId: Long,
    ) : ObjectType
    data class CommentObject(
        val comment: Comment,
    ) : ObjectType
}

@HiltViewModel
class PosterViewModel @Inject constructor(
    private val posterRepo: PosterRepo,
    private val reactionRepo: ReactionRepo,
    private val uploadRepo: UploadRepo,
) : ViewModel() {
    private val _getPosterStateFlow = MutableStateFlow<SimpleDataState<GetPosterDataModel.Response>?>(null)
    val getPosterStateFlow = _getPosterStateFlow.asStateFlow()

    private val _commentState = object : RefreshAndLoadMoreStatesCombinedOne<Long, Comment>(viewModelScope) {
        override fun refresh(data: Long) = refresh {
            posterRepo.getCommentsById(data)
        }
        override fun loadMore(data: Long) = loadMore {
            posterRepo.getCommentsById(data, it.toInt())
        }

    }
    val commentStateExports = _commentState.export()

    private val _subCommentState = object : RefreshAndLoadMoreStatesCombinedOne<Long, Comment>(viewModelScope) {
        override fun refresh(data: Long) = refresh {
            posterRepo.getCommentsOfCommentById(data)
        }
        override fun loadMore(data: Long) = loadMore {
            posterRepo.getCommentsOfCommentById(data, it.toInt())
        }
    }
    val subCommentStateExports = _subCommentState.export()

    private val _commentEditDataMapFlow = MutableStateFlow<Map<CommentType, CommentEditData?>>(emptyMap())
    val commentEditDataMapFlow = _commentEditDataMapFlow.asStateFlow()

    private val _likingsFlow = MutableStateFlow<Set<ObjectType>>(emptySet())
    val likingsFlow = _likingsFlow.asStateFlow()

    private val _sendCommentStateFlow = MutableStateFlow<SimpleState?>(null)
    val sendCommentStateFlow = _sendCommentStateFlow.asStateFlow()

    private val _deletePosterStateFlow = MutableStateFlow<SimpleState?>(null)
    val deletePosterStateFlow = _deletePosterStateFlow.asStateFlow()

    private val _deleteCommentStateFlow = MutableStateFlow<SimpleState?>(null)
    val deleteCommentStateFlow = _deleteCommentStateFlow.asStateFlow()

    /**
     * 清空状态
     */
    fun clearStates() {
        _sendCommentStateFlow.value = _sendCommentStateFlow.value.cleared()
        _deleteCommentStateFlow.value = _deleteCommentStateFlow.value.cleared()
        _deletePosterStateFlow.value = _deletePosterStateFlow.value.cleared()
    }

    /**
     * 获取帖子
     */
    fun getPosterById(id: Long) {
        _getPosterStateFlow.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val poster = posterRepo.getPosterById(id)
                _getPosterStateFlow.value = SimpleDataState.Success(poster)
            } catch (e: Exception) {
                _getPosterStateFlow.value = SimpleDataState.Fail()
            }
        }
    }

    /**
     * 编辑评论
     */
    fun setCommentEditData(
        commentType: CommentType,
        commentEditData: CommentEditData
    ) {
        val ifUpload = commentEditData.uploadImageData.images.isNotEmpty()
        _commentEditDataMapFlow.value = _commentEditDataMapFlow.value.plus(Pair(commentType, commentEditData.copy(
            uploadImageData = commentEditData.uploadImageData.copy(
                ifUpload = ifUpload
            )
        )))
    }

    fun like(objectType: ObjectType) {
        _likingsFlow.value = _likingsFlow.value.plus(objectType)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when(objectType) {
                    is ObjectType.PosterObject -> {
                        val res = reactionRepo.likePoster(objectType.posterId)
                        // 更新帖子的点赞状态
                        _getPosterStateFlow.value = (_getPosterStateFlow.value as SimpleDataState.Success).copy(
                            data = (_getPosterStateFlow.value as SimpleDataState.Success).data.copy(
                                like = res.like,
                                likeNum = res.likeNum
                            )
                        )
                    }
                    is ObjectType.CommentObject -> {
                        val res = reactionRepo.likeComment(objectType.comment.id.toLong())

                        // 更新评论的点赞状态
                        _commentState.data = changeLike(
                            comments = _commentState.data,
                            id = objectType.comment.id.toLong(),
                            like = res.like,
                            likeNum = res.likeNum,
                        )

                        // 子评论列表也要更新
                        _subCommentState.data = changeLike(
                            comments = _subCommentState.data,
                            id = objectType.comment.id.toLong(),
                            like = res.like,
                            likeNum = res.likeNum,
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _likingsFlow.value = _likingsFlow.value.minus(objectType)
        }
    }

    private fun setUploadImageState(
        commentType: CommentType,
        uri: Uri,
        uploadImageState: UploadImageState,
    ) {
        val commentEditDataMap = commentEditDataMapFlow.value
        val commentEditData = commentEditDataMap[commentType] ?: CommentEditData.empty()

        val images = if(commentEditData.uploadImageData.images.firstOrNull {
            it.imageData is ImageData.Local && it.imageData.uri == uri
        } == null) {
            commentEditData.uploadImageData.images.plus(
                ImageDataWithUploadState(
                    imageData = ImageData.Local(uri),
                    uploadImageState = uploadImageState
                )
            )
        } else {
            commentEditData.uploadImageData.images.map {
                if(it.imageData is ImageData.Local && it.imageData.uri == uri) it.copy(
                    uploadImageState = uploadImageState
                ) else it
            }
        }

        setCommentEditData(commentType, commentEditData.copy(
            uploadImageData = commentEditData.uploadImageData.copy(images = images)
        ))
    }

    /**
     * 上传评论中的图片
     */
    fun uploadImage(
        context: Context,
        commentType: CommentType,
        uri: Uri
    ) {
        // 先添加到列表中，再上传
        setUploadImageState(commentType, uri, UploadImageState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = uploadRepo.uploadImage(context, uri)

                // 上传成功后，更新列表
                setUploadImageState(commentType, uri, UploadImageState.Success(res))
            } catch (e: Exception) {
                e.printStackTrace()

                // 上传失败后，更新列表
                setUploadImageState(commentType, uri, UploadImageState.Fail)
            }
        }
    }

    private suspend fun sendCommentMethod(
        commentType: CommentType,
        editData: CommentEditData
    ) = when(commentType) {
        is CommentType.ToPoster -> {
            reactionRepo.sendCommentToPoster(
                id = commentType.posterId,
                text = editData.text,
                replyUid = 0,
                anonymous = editData.anonymous,
                images = if(editData.uploadImageData.ifUpload) editData.uploadImageData.images.mapNotNull {
                    if(it.uploadImageState is UploadImageState.Success) it.uploadImageState.image.mid
                    else null
                } else emptyList()
            )
        }
        is CommentType.ToComment -> {
            reactionRepo.sendCommentToComment(
                id = commentType.mainComment.id.toLong(),
                text = editData.text,
                replyComment = commentType.subComment,
                anonymous = editData.anonymous,
                images = if (editData.uploadImageData.ifUpload) editData.uploadImageData.images.mapNotNull {
                    if (it.uploadImageState is UploadImageState.Success) it.uploadImageState.image.mid
                    else null
                } else emptyList()
            )
        }
    }

    private fun insertNewComment(
        commentType: CommentType,
        comment: Comment,
    ) {
        when(commentType) {
            // 对帖子的评论
            is CommentType.ToPoster -> {
                // 插入新的评论到评论区的第一个
                _commentState.data = _commentState.data.toMutableList().apply {
                    add(0, comment)
                }

                // 更新帖子的评论数
                (getPosterStateFlow.value as? SimpleDataState.Success<GetPosterDataModel.Response>)?.let {
                    _getPosterStateFlow.value = it.copy(
                        data = it.data.copy(
                            commentNum = it.data.commentNum + 1
                        )
                    )
                }
            }
            // 对评论的评论
            is CommentType.ToComment -> {
                // 插入到子评论的第一个
                val subComments = _subCommentState.data
                _subCommentState.data = subComments.toMutableList().apply {
                    add(0, comment)
                }

                // 插入到帖子评论的评论列表中
                val allComments = _commentState.data
                _commentState.data = addCommentToComment(
                    allComments, commentType.mainComment, comment
                )
            }
        }
    }

    /**
     * 发送评论
     */
    fun sendComment(
        commentType: CommentType,
        editData: CommentEditData,
    ) {
        _sendCommentStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 发送请求
                val comment = sendCommentMethod(commentType, editData)

                // 发送成功后，清空编辑框
                setCommentEditData(commentType, CommentEditData.empty())

                // 插入新的评论
                insertNewComment(commentType, comment)

                // 发送成功后，更新状态
                _sendCommentStateFlow.value = SimpleState.Success

            } catch (e: Exception) {
                e.printStackTrace()

                // 失败
                _sendCommentStateFlow.value = SimpleState.Fail
            }
        }
    }

    /**
     * 删除帖子
     */
    fun deletePosterById(id: Long) {
        _deleteCommentStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                posterRepo.deletePosterById(id)
                _deleteCommentStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _deleteCommentStateFlow.value = SimpleState.Fail
            }
        }
    }

    /**
     * 删除评论
     */
    fun deleteCommentById(id: Long) {
        _deleteCommentStateFlow.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reactionRepo.deleteComment(id)
                _commentState.data = deleteComment(_commentState.data, id)
                _deleteCommentStateFlow.value = SimpleState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _deleteCommentStateFlow.value = SimpleState.Fail
            }
        }
    }

    /**
     * 删除评论中的图片
     */
    fun deleteImageOfComment(
        commentType: CommentType,
        index: Int,
    ) {
        val commentEditDataMap = commentEditDataMapFlow.value
        val commentEditData = commentEditDataMap[commentType] ?: return
        setCommentEditData(commentType, commentEditData.copy(
            uploadImageData = commentEditData.uploadImageData.copy(
                images = commentEditData.uploadImageData.images.filterIndexed { i, _ -> i != index }
            )
        ))
    }
}