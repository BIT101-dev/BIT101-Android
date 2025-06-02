package cn.bit101.android.data.repo.base

import android.content.Context
import android.net.Uri
import cn.bit101.api.model.common.Image

interface UploadRepo {

    /**
     * 上传图片
     */
    suspend fun uploadImage(imageUri: Uri): Image

}