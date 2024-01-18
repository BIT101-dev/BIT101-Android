package cn.bit101.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

object FileUtils {
    suspend fun getFileFromUri(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_image_file")
        tempFile.outputStream().use { fileOut ->
            inputStream?.copyTo(fileOut)
        }
        inputStream?.close()
        tempFile
    }
}