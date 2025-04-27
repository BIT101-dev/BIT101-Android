package cn.bit101.android.features.poster

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import cn.bit101.android.features.common.MainController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class ImageDownloader {
    private val client = OkHttpClient
        .Builder()
        .build()

    private val contentValuesNormal = ContentValues()
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BIT101/")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

    fun downloadAndAddImage(
        url: String,
        ctx: Context,
        mainController: MainController,
        callback: ()->Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            async(Dispatchers.IO) {
                val request = Request
                    .Builder()
                    .url(url)
                    .build()

                val contentValues = contentValuesNormal.apply {
                    val extensionName = url.substringAfterLast('.')

                    // 总感觉 101 上的图片都是 .jpeg, 所以这里似乎没必要
                    // 但还是先这么写着了
                    put(MediaStore.Images.Media.MIME_TYPE, when(extensionName) {
                        "png" -> "image/png"
                        "gif" -> "image/gif"
                        else -> "image/jpeg"
                    })

                    put(MediaStore.Images.Media.DISPLAY_NAME, "BIT101_IMG_${System.currentTimeMillis()}.$extensionName")
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
        } catch (e:Exception) {
            mainController.snackbar("图片保存失败Orz")
        }
    }
}