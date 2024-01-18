package cn.bit101.android.repo.base

import android.content.Context
import android.net.Uri
import cn.bit101.api.model.common.Image

interface UploadRepo {

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
    ): Image

}