package cn.bit101.android.features.postedit

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.data.repo.base.UploadRepo
import cn.bit101.android.features.common.helper.ImageData
import cn.bit101.android.features.common.helper.ImageDataWithUploadState
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.UploadImageData
import cn.bit101.android.features.common.helper.UploadImageState
import cn.bit101.android.features.common.helper.withSimpleDataStateLiveData
import cn.bit101.android.features.common.helper.withSimpleStateFlow
import cn.bit101.api.model.common.Claim
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

data class EditPosterData(
    val anonymous: Boolean,
    val claim: Claim?,
    val uploadImageData: UploadImageData,
    val public: Boolean,
    val tags: List<String>,
    val text: String,
    val title: String,
) : Serializable {
    companion object {
        val default = EditPosterData(
            anonymous = false,
            claim = null,
            uploadImageData = UploadImageData.default,
            public = true,
            tags = emptyList(),
            text = "",
            title = "",
        )
    }

    fun isEmpty(): Boolean {
        return text.isEmpty() || title.isEmpty() || claim == null
    }
}

@HiltViewModel
class PostEditViewModel @Inject constructor(
    private val posterRepo: PosterRepo,
    private val uploadRepo: UploadRepo,
) : ViewModel() {

    private val _loadPosterFlow = MutableStateFlow<SimpleState?>(null)
    val loadPosterFlow = _loadPosterFlow.asStateFlow()

    val getClaimsStateLiveData = MutableLiveData<SimpleDataState<List<Claim>>?>(null)

    val postStateLiveData = MutableLiveData<SimpleDataState<Long>?>(null)

    private val _editPosterDataFlow = MutableStateFlow(EditPosterData.default)
    val editPosterDataFlow = _editPosterDataFlow.asStateFlow()

    fun setEditData(editData: EditPosterData) {
        _editPosterDataFlow.value = editData
    }

    fun deleteImage(idx: Int) {
        val oldEditData = editPosterDataFlow.value
        _editPosterDataFlow.value = oldEditData.copy(
            uploadImageData = oldEditData.uploadImageData.copy(
                images = oldEditData.uploadImageData.images.filterIndexed { index, _ -> index != idx }
            )
        )
    }

    fun loadPoster(id: Long?) = withSimpleStateFlow(_loadPosterFlow) {
        if(id == null) return@withSimpleStateFlow

        val poster = posterRepo.getPosterById(id)
        _editPosterDataFlow.value = EditPosterData(
            anonymous = poster.anonymous,
            claim = poster.claim,
            uploadImageData = UploadImageData(
                true,
                poster.images.map {
                    ImageDataWithUploadState(
                        imageData = ImageData.Remote(it),
                        uploadImageState = UploadImageState.Success(it)
                    )
                }
            ),
            public = poster.public,
            tags = poster.tags,
            text = poster.text,
            title = poster.title,
        )
    }

    fun loadClaim() = withSimpleDataStateLiveData(getClaimsStateLiveData) {
        posterRepo.getClaims()
    }

    fun uploadImage(uri: Uri) {
        val oldEditData = editPosterDataFlow.value

        _editPosterDataFlow.value = oldEditData.copy(
            uploadImageData = oldEditData.uploadImageData.copy(
                images = oldEditData.uploadImageData.images.plus(
                    ImageDataWithUploadState(
                        imageData = ImageData.Local(uri),
                        uploadImageState = UploadImageState.Loading
                    )
                )
            )
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = uploadRepo.uploadImage(uri)
                val oldEditData = editPosterDataFlow.value
                _editPosterDataFlow.value = oldEditData.copy(
                    uploadImageData = oldEditData.uploadImageData.copy(
                        images = oldEditData.uploadImageData.images.map {
                            if(it.imageData is ImageData.Local && (it.imageData as ImageData.Local).uri == uri) ImageDataWithUploadState(
                                imageData = ImageData.Remote(image),
                                uploadImageState = UploadImageState.Success(image)
                            ) else it
                        }
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                val oldEditData = editPosterDataFlow.value
                _editPosterDataFlow.value = oldEditData.copy(
                    uploadImageData = oldEditData.uploadImageData.copy(
                        images = oldEditData.uploadImageData.images.map {
                            if(it.imageData is ImageData.Local && (it.imageData as ImageData.Local).uri == uri) ImageDataWithUploadState(
                                imageData = ImageData.Local(uri),
                                uploadImageState = UploadImageState.Fail
                            ) else it
                        }
                    )
                )
            }
        }
    }

    fun post() = withSimpleDataStateLiveData(postStateLiveData) {
        val editData = editPosterDataFlow.value

        editData.uploadImageData.images.forEach {
            if(it.uploadImageState !is UploadImageState.Success) throw Exception("upload image error")
        }

        posterRepo.post(
            anonymous = editData.anonymous,
            claimId = editData.claim?.id ?: 0,
            imageMids = editData.uploadImageData.images.map {
                (it.uploadImageState as UploadImageState.Success).image.mid
            },
            public = editData.public,
            tags = editData.tags,
            text = editData.text,
            title = editData.title,
        ).id.toLong()
    }

    fun put(id: Long) = withSimpleDataStateLiveData(postStateLiveData) {
        val editData = editPosterDataFlow.value

        editData.uploadImageData.images.forEach {
            if(it.uploadImageState !is UploadImageState.Success) throw Exception("upload image error")
        }
        posterRepo.update(
            id = id,
            anonymous = editData.anonymous,
            claimId = editData.claim?.id ?: 0,
            imageMids = editData.uploadImageData.images.map {
                (it.uploadImageState as UploadImageState.Success).image.mid
            },
            public = editData.public,
            tags = editData.tags,
            text = editData.text,
            title = editData.title,
        )
        id
    }
}