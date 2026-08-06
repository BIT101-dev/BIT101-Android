package cn.bit101.android.features.setting.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * 读取 logcat 并写入文件，通过系统分享发送出去，便于测试者反馈问题
 */
object LogExporter {
    private const val TAG = "LogExporter"
    private const val LOG_FILE_NAME = "BIT101.log"

    /**
     * 导出日志文件，成功返回文件，失败返回 null
     */
    fun export(context: Context): File? {
        return try {
            val log = readLogcat()
            val file = File(context.getExternalFilesDir("logcat"), LOG_FILE_NAME)
            file.parentFile?.mkdirs()
            file.writeText(log)
            file
        } catch (e: Exception) {
            Log.e(TAG, "导出日志失败", e)
            null
        }
    }

    /**
     * 通过系统分享发送日志文件
     */
    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享日志"))
    }

    private fun readLogcat(): String {
        return try {
            // --uid 过滤整个应用的 uid，覆盖应用所有进程（各进程共享同一 uid）
            // -b main -b crash 同时读取主缓冲与崩溃缓冲，避免崩溃日志滚动丢失
            // 不加 -t/-T，导出缓冲中该应用的全部日志
            val command = listOf(
                "logcat",
                "-d",
                "-b",
                "main",
                "-b",
                "crash",
                "-v",
                "threadtime",
                "--uid=${android.os.Process.myUid()}",
            )
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            output
        } catch (e: IOException) {
            Log.e(TAG, "读取 logcat 失败", e)
            "读取 logcat 失败: ${e.message}"
        }
    }
}
