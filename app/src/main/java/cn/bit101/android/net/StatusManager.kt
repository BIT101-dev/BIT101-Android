package cn.bit101.android.net

import android.util.Log
import cn.bit101.android.database.DataStore
import cn.bit101.android.database.EncryptedStore
import cn.bit101.android.net.school.checkLogin
import cn.bit101.android.net.school.login
import cn.bit101.android.viewmodel.updateLexueCalendar

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
        Log.i("StatusManager", "sid: $sid, password: $password")
        if (sid != null && password != null) {
            // 可能是Cookie过期 尝试重新登录
            login(sid, password)
        }
    }

    // 更新乐学日程
    DataStore.lexueCalendarUrlFlow.collect {
        if (it == null) return@collect
        updateLexueCalendar()
    }
}