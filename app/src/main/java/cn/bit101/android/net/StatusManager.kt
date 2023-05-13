package cn.bit101.android.net

import cn.bit101.android.database.DataStore
import cn.bit101.android.net.school.checkLogin
import cn.bit101.android.viewmodel.updateLexueCalendar

/**
 * @author flwfdd
 * @date 2023/5/10
 * @description _(:з」∠)_
 */

suspend fun updateStatus() {
    // 检测登录状态
    checkLogin()

    // 更新乐学日程
    DataStore.lexueCalendarUrlFlow.collect{
        if (it == null) return@collect
        updateLexueCalendar()
    }
}