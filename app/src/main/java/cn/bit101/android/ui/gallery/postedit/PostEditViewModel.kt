package cn.bit101.android.ui.gallery.postedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.repo.base.UploadRepo
import cn.bit101.android.ui.common.ImageData
import cn.bit101.android.ui.common.ImageDataWithUploadState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.UploadImageData
import cn.bit101.android.ui.common.UploadImageState
import cn.bit101.api.model.common.Claim
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GetClaimsState {
    object Loading : GetClaimsState
    data class Success(val claims: List<Claim>) : GetClaimsState
    object Error : GetClaimsState
}

sealed interface PutOrPostPosterState {
    object Loading: PutOrPostPosterState
    data class Success(val id: Long): PutOrPostPosterState
    object Error: PutOrPostPosterState
}

@HiltViewModel
class PostEditViewModel @Inject constructor(
    private val posterRepo: PosterRepo,
    private val uploadRepo: UploadRepo,
) : ViewModel() {

    private val _loadPosterFlow = MutableStateFlow<SimpleState?>(null)
    val loadPosterFlow = _loadPosterFlow.asStateFlow()

    private val _titleFlow = MutableStateFlow("")
    val titleFlow = _titleFlow.asStateFlow()

    private val _textFlow = MutableStateFlow("")
    val textFlow = _textFlow.asStateFlow()

    private val _tagsFlow = MutableStateFlow<List<String>>(emptyList())
    val tagsFlow = _tagsFlow.asStateFlow()

    private val _anonymousFlow = MutableStateFlow(false)
    val anonymousFlow = _anonymousFlow.asStateFlow()

    private val _publicFlow = MutableStateFlow(true)
    val publicFlow = _publicFlow.asStateFlow()

    val getClaimsStateLiveData = MutableLiveData<GetClaimsState>(null)

    private val _uploadImagesStateFlow = MutableStateFlow(UploadImageData(true, emptyList()))
    val uploadImagesStateFlow = _uploadImagesStateFlow.asStateFlow()

    private val _claimFlow = MutableStateFlow<Claim?>(null)
    val claimFlow = _claimFlow.asStateFlow()

    val postStateLiveData = MutableLiveData<PutOrPostPosterState>(null)


    fun setTitle(title: String) { _titleFlow.value = title }
    fun setText(text: String) { _textFlow.value = text }
    fun setTags(tags: List<String>) { _tagsFlow.value = tags }
    fun setClaim(claim: Claim) { _claimFlow.value = claim }
    fun setAnonymous(anonymous: Boolean) { _anonymousFlow.value = anonymous }
    fun setPublic(public: Boolean) { _publicFlow.value = public }

    fun deleteImage(idx: Int) {
        _uploadImagesStateFlow.value = uploadImagesStateFlow.value.copy(
            images = uploadImagesStateFlow.value.images.filterIndexed { index, _ -> index != idx }
        )
    }

    fun loadPoster(id: Long?) {
        if(id == null) {
            _loadPosterFlow.value = SimpleState.Success
        } else {
            _loadPosterFlow.value = SimpleState.Loading
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val poster = posterRepo.getPosterById(id)
                    _titleFlow.value = poster.title
                    _textFlow.value = poster.text
                    _tagsFlow.value = poster.tags
                    _claimFlow.value = poster.claim
                    _uploadImagesStateFlow.value = UploadImageData(
                        true,
                        poster.images.map {
                            ImageDataWithUploadState(
                                imageData = ImageData.Remote(it),
                                uploadImageState = UploadImageState.Success(it)
                            )
                        }
                    )
                    _anonymousFlow.value = poster.anonymous
                    _publicFlow.value = poster.public
                    _loadPosterFlow.value = SimpleState.Success
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loadPosterFlow.value = SimpleState.Fail
                }
            }
        }
    }

    fun loadClaim() {
        getClaimsStateLiveData.value = GetClaimsState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val claims = posterRepo.getClaim()
                getClaimsStateLiveData.postValue(GetClaimsState.Success(claims))
            } catch (e: Exception) {
                e.printStackTrace()
                getClaimsStateLiveData.postValue(GetClaimsState.Error)
            }
        }
    }

    fun uploadImage(context: Context, uri: Uri) {
        _uploadImagesStateFlow.value = uploadImagesStateFlow.value.copy(
            images = uploadImagesStateFlow.value.images.plus(
                ImageDataWithUploadState(
                    imageData = ImageData.Local(uri),
                    uploadImageState = UploadImageState.Loading
                )
            )
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = uploadRepo.uploadImage(context, uri)
                _uploadImagesStateFlow.value = uploadImagesStateFlow.value.copy(
                    images = uploadImagesStateFlow.value.images.map {
                        if(it.imageData is ImageData.Local && it.imageData.uri == uri) ImageDataWithUploadState(
                            imageData = ImageData.Local(uri),
                            uploadImageState = UploadImageState.Success(image)
                        ) else it
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadImagesStateFlow.value = uploadImagesStateFlow.value.copy(
                    images = uploadImagesStateFlow.value.images.map {
                        if(it.imageData is ImageData.Local && it.imageData.uri == uri) ImageDataWithUploadState(
                            imageData = ImageData.Local(uri),
                            uploadImageState = UploadImageState.Fail
                        ) else it
                    }
                )
            }
        }
    }

    fun post(
        anonymous: Boolean,
        claim: Claim,
        uploadImageData: UploadImageData,
        public: Boolean,
        tags: List<String>,
        text: String,
        title: String,
    ) {
        postStateLiveData.value = PutOrPostPosterState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uploadImageData.images.forEach {
                    if(it.uploadImageState !is UploadImageState.Success) throw Exception("upload image error")
                }
                val id = posterRepo.post(
                    anonymous = anonymous,
                    claimId = claim.id,
                    imageMids = uploadImageData.images.map {
                        (it.uploadImageState as UploadImageState.Success).image.mid
                    },
                    public = public,
                    tags = tags,
                    text = text,
                    title = title,
                ).id.toLong()

                postStateLiveData.postValue(PutOrPostPosterState.Success(id))
            } catch (e: Exception) {
                e.printStackTrace()
                postStateLiveData.postValue(PutOrPostPosterState.Error)
            }
        }
    }

    fun put(
        id: Long,
        anonymous: Boolean,
        claim: Claim,
        uploadImageData: UploadImageData,
        public: Boolean,
        tags: List<String>,
        text: String,
        title: String,
    ) {
        postStateLiveData.value = PutOrPostPosterState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uploadImageData.images.forEach {
                    if(it.uploadImageState !is UploadImageState.Success) throw Exception("upload image error")
                }
                posterRepo.update(
                    id = id,
                    anonymous = anonymous,
                    claimId = claim.id,
                    imageMids = uploadImageData.images.map {
                        (it.uploadImageState as UploadImageState.Success).image.mid
                    },
                    public = public,
                    tags = tags,
                    text = text,
                    title = title,
                )

                postStateLiveData.postValue(PutOrPostPosterState.Success(id))
            } catch (e: Exception) {
                e.printStackTrace()
                postStateLiveData.postValue(PutOrPostPosterState.Error)
            }
        }
    }
}