package cn.bit101.android.data.repo

import android.content.Context
import android.net.Uri
import cn.bit101.android.data.common.FileUtils
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.UploadRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

internal class DefaultUploadRepo @Inject constructor(
    private val apiManager: APIManager,
    @ApplicationContext private val context: Context,
) : UploadRepo {

    private val api = apiManager.api

    override suspend fun uploadImage(imageUri: Uri) = withContext(Dispatchers.IO) {

        // 从Uri中获取文件
        val file = FileUtils.getFileFromUri(context, imageUri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestFile)


       api.upload.upload(part).body() ?: throw Exception("图片上传失败")
    }

}