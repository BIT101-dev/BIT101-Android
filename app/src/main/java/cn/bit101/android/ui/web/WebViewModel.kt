package cn.bit101.android.ui.web

import androidx.lifecycle.ViewModel
import cn.bit101.android.manager.base.LoginStatusManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class WebViewModel @Inject constructor(
    private val loginStatusManager: LoginStatusManager
) : ViewModel() {
    val BASE_URL = "https://bit101.cn"

    val fakeCookie = loginStatusManager.fakeCookie
    val sid = loginStatusManager.sid
    val password = loginStatusManager.password


}