package cn.bit101.api.option

import okhttp3.OkHttpClient

data class ApiOption(
    val bit101Client: OkHttpClient,
    val schoolClient: OkHttpClient,

    val webVpnUrls: ApiUrlOption,
    val localUrls: ApiUrlOption,

    val webVpn: Boolean = false,
    val bit101Headers: Map<String, String> = emptyMap(),
)
