package cn.bit101.android.data.net.base

import cn.bit101.api.Bit101Api


internal interface APIManager {
    /**
     * BIT101 API
     */
    val api: Bit101Api

    /**
     * 切换是否使用 WebVPN
     */
    fun switch(webVpn: Boolean)

    /**
     * 切换到 WebVPN
     */
    fun switchToWebVpn() = switch(true)

    /**
     * 切换到校园网
     */
    fun switchToLocal() = switch(false)

}

