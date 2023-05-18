package cn.bit101.android.net

import android.util.Log
import cn.bit101.android.database.DataStore
import cn.bit101.android.database.EncryptedStore
import cn.bit101.android.net.bit101.BIT101Service
import cn.bit101.android.net.bit101.loginBIT101
import cn.bit101.android.net.school.checkLogin
import cn.bit101.android.net.school.login
import cn.bit101.android.viewmodel.updateLexueCalendar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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