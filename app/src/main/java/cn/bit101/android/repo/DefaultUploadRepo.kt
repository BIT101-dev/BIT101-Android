package cn.bit101.android.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.UploadRepo
import cn.bit101.android.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject


class DefaultUploadRepo @Inject constructor(

) : UploadRepo {
    override suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
    ) = withContext(Dispatchers.IO) {

        // 从Uri中获取文件
        val file = FileUtils.getFileFromUri(context, imageUri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestFile)


       BIT101API.upload.upload(part).body() ?: throw Exception("图片上传失败")
    }

}