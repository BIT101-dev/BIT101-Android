package cn.bit101.android.net

import cn.bit101.android.net.school.checkLogin


suspend fun updateStatus(){
    // 检测登录状态
    checkLogin()
}