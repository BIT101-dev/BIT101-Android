package cn.bit101.android.features.web

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.user.base.LoginStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
internal class WebViewModel @Inject constructor(
    private val loginStatus: LoginStatus
) : ViewModel() {
    val BASE_URL = "https://bit101.cn"

    val fakeCookie = loginStatus.fakeCookie
    val sid = loginStatus.sid
    val password = loginStatus.password


}