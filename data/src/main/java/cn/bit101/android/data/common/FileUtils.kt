package cn.bit101.android.data.common

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal object FileUtils {
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