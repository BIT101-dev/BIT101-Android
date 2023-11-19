package cn.bit101.api.option

import okhttp3.OkHttpClient

val DEFAULT_WEB_VPN_URLS = ApiUrlOption(
    bit101Url = "https://bit101.flwfdd.xyz/",
    jwmsUrl = "https://webvpn.bit.edu.cn/http/77726476706e69737468656265737421fae04c8f69326144300d8db9d6562d/",
    jwcUrl = "https://webvpn.bit.edu.cn/https/77726476706e69737468656265737421fae042d225397c1e7b0c9ce29b5b/",
    jxzxehallappUrl = "https://webvpn.bit.edu.cn/https/77726476706e69737468656265737421faef5b842238695c720999bcd6572a216b231105adc27d/",
    lexueUrl = "https://webvpn.bit.edu.cn/https/77726476706e69737468656265737421fcf25989227e6a596a468ca88d1b203b/",
    androidUrl = "http://android.bit101.cn/",
    schoolLoginUrl = "https://login.bit.edu.cn"
)

val DEFAULT_LOCAL_URLS = ApiUrlOption(
    bit101Url = "https://bit101.flwfdd.xyz",
    jwmsUrl = "http://jwms.bit.edu.cn",
    jwcUrl = "https://jwc.bit.edu.cn",
    jxzxehallappUrl = "https://jxzxehallapp.bit.edu.cn",
    lexueUrl = "https://lexue.bit.edu.cn",
    androidUrl = "http://android.bit101.cn",
    schoolLoginUrl = "https://login.bit.edu.cn"
)

val DEV_URLS = ApiUrlOption(
    bit101Url = "https://dev.bit101.flwfdd.xyz",
    jwmsUrl = "http://jwms.bit.edu.cn",
    jwcUrl = "https://jwc.bit.edu.cn",
    jxzxehallappUrl = "https://jxzxehallapp.bit.edu.cn",
    lexueUrl = "https://lexue.bit.edu.cn",
    androidUrl = "http://android.bit101.cn",
    schoolLoginUrl = "https://login.bit.edu.cn"
)


val DEFAULT_API_OPTION = ApiOption(
    webVpn = false,
    bit101Client = OkHttpClient(),
    schoolClient = OkHttpClient(),
    localUrls = DEV_URLS,
    webVpnUrls = DEV_URLS,
)