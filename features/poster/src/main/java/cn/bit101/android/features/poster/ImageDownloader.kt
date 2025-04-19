package cn.bit101.android.features.poster

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import cn.bit101.android.features.common.MainController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class ImageDownloader {
    private val client = OkHttpClient.Builder().build()
    private val contentValuesNormal = ContentValues().apply {
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BIT101/")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    fun downloadAndAddImage(url: String, ctx: Context, mainController: MainController, callback: ()->Unit)
    = CoroutineScope(Dispatchers.Main).launch {
        try {
            async(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()

                val contentValues = contentValuesNormal.apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "BIT101_IMG_${System.currentTimeMillis()}.jpg")
                }

                val resolver = ctx.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Failed to create MediaStore entry")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    client.newCall(request).execute().body?.byteStream()?.use { inputStream ->
                        inputStream.copyTo(outputStream)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(uri, contentValues, null, null)
                        }
                    }
                }
            }.await()
            mainController.snackbar("图片已保存到相册!")
            callback()
        }catch (e:Exception){
            mainController.snackbar("图片保存失败Orz")
        }
    }
}