package cn.bit101.android.net

import android.util.Log
import cn.bit101.android.database.DataStore
import cn.bit101.android.database.EncryptedStore
import cn.bit101.android.net.bit101.BIT101Service
import cn.bit101.android.net.bit101.loginBIT101
import cn.bit101.android.net.school.checkLogin
import cn.bit101.android.net.school.login
import cn.bit101.android.viewmodel.updateLexueCalendar
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * @author flwfdd
 * @date 2023/5/10
 * @description 状态管理
 * _(:з」∠)_
 */

suspend fun updateStatus() {
    // 检测登录状态
    if (!checkLogin()) {
        val sid = EncryptedStore.getString(EncryptedStore.SID)
        val password = EncryptedStore.getString(EncryptedStore.PASSWORD)
        if (sid != null && password != null) {
            // 可能是Cookie过期 尝试重新登录
            login(sid, password)
        }
    }

    // 检测BIT101登陆状态
    try {
        if (!BIT101Service.service.check().isSuccessful) {
            // 可能是Cookie过期 尝试重新登录
            val sid = EncryptedStore.getString(EncryptedStore.SID)
            val password = EncryptedStore.getString(EncryptedStore.PASSWORD)
            if (sid != null && password != null) {
                loginBIT101(sid, password)
            }
        }
    } catch (e: Exception) {
        Log.e("StatusManager", "BIT101 check error ${e.message}")
    }


    // 更新BIT101登陆状态
    MainScope().launch {
        var lastSid = DataStore.loginSidFlow.first()
        DataStore.loginSidFlow.collect {
            if (it.isNullOrBlank()) {
                lastSid = ""
                DataStore.setString(DataStore.FAKE_COOKIE, "")
                return@collect
            }
            if (lastSid == it) return@collect
            lastSid = it
            val sid = EncryptedStore.getString(EncryptedStore.SID)
            val password = EncryptedStore.getString(EncryptedStore.PASSWORD)
            if (sid != null && password != null) try {
                loginBIT101(sid, password)
            } catch (e: Exception) {
                Log.e("StatusManager", "BIT101 login error ${e.message}")
            }
        }
    }

    // 更新乐学日程
    MainScope().launch {
        DataStore.lexueCalendarUrlFlow.collect {
            if (it == null) return@collect
            updateLexueCalendar()
        }
    }
}

// 版本更新信息
data class VersionInfo(
    val version_code: Int, // 最新版本号
    val version_name: String, // 最新版本名 形如x.x.x
    val msg: String, // 备注信息
    val url: String, // 最新版本下载链接
)

// 获取版本更新信息
suspend fun getVersionInfo(): VersionInfo? {
    try {
        return withContext(Dispatchers.IO) {
            val url = "http://android.bit101.cn/version"
            val client = HttpClient.client
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use {
                return@withContext Gson().fromJson(it.body?.string(), VersionInfo::class.java)
            }
        }
    } catch (e: Exception) {
        Log.e("StatusManager", "getVersionInfo error ${e.message}")
        return null
    }
}
